package com.example.seckill.seckill.service.impl;

import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.service.ActivityService;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.mq.SeckillMessage;
import com.example.seckill.mq.SeckillProducer;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.service.OrderService;
import com.example.seckill.seckill.dto.SeckillResultDTO;
import com.example.seckill.seckill.metrics.SeckillMetrics;
import com.example.seckill.seckill.service.SeckillService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀核心服务实现。
 *
 * <p>核心流程：
 * <ol>
 *   <li>校验活动合法性（存在、状态、时间窗口）</li>
 *   <li>执行 Redis Lua 脚本，保证"判重+判库存+扣减"原子性</li>
 *   <li>Lua 返回 0 → 写 result=processing，投递 MQ 异步建单</li>
 *   <li>Lua 返回 1 → 抛重复购买异常</li>
 *   <li>Lua 返回 2 → 抛库存不足异常</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ActivityService activityService;
    private final SeckillProducer seckillProducer;
    private final OrderService orderService;
    private final SeckillMetrics seckillMetrics;

    private static final long RESULT_TTL_SECONDS = 300;

    @Value("${seckill.stock-key-prefix}")
    private String stockKeyPrefix;

    @Value("${seckill.user-mark-key-prefix}")
    private String userMarkKeyPrefix;

    @Value("${seckill.result-key-prefix}")
    private String resultKeyPrefix;

    /** Lua 脚本对象，应用启动时加载一次，避免每次请求重复 IO */
    private DefaultRedisScript<Long> stockDeductScript;

    @PostConstruct
    public void initScript() {
        stockDeductScript = new DefaultRedisScript<>();
        stockDeductScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua/stock_deduct.lua")));
        stockDeductScript.setResultType(Long.class);
    }

    // -------------------------------------------------------------------------
    // doSeckill
    // -------------------------------------------------------------------------

    @Override
    public void doSeckill(Long activityId, Long userId) {

        // 1. 取活动
        SeckillActivity activity = activityService.getActivity(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 校验活动状态与时间窗口
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStatus() != 1 || now.isBefore(activity.getStartTime())) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_STARTED);
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.ACTIVITY_ENDED);
        }

        // 3. 拼装 Redis key
        String stockKey    = stockKeyPrefix    + activityId;
        String userMarkKey = userMarkKeyPrefix  + activityId;
        String resultKey   = resultKeyPrefix    + activityId + ":" + userId;

        // 4. 执行 Lua 脚本（原子扣减）
        Long luaResult = redisTemplate.execute(
                stockDeductScript,
                Arrays.asList(stockKey, userMarkKey),
                userId.toString()
        );

        log.info("[SeckillService] activityId={}, userId={}, luaResult={}",
                activityId, userId, luaResult);

        if (luaResult == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        // 5. 根据 Lua 返回值分支处理
        if (luaResult == 1L) {
            seckillMetrics.recordSeckill("repeat");
            throw new BusinessException(ErrorCode.SECKILL_REPEAT);
        }
        if (luaResult == 2L) {
            seckillMetrics.recordSeckill("stock_empty");
            throw new BusinessException(ErrorCode.SECKILL_STOCK_EMPTY);
        }

        // luaResult == 0：秒杀成功
        // 6. 写 result key 为 "processing"，60 秒超时
        redisTemplate.opsForValue().set(resultKey, "processing", RESULT_TTL_SECONDS, TimeUnit.SECONDS);

        // 7. 投递 MQ 异步建单
        SeckillMessage msg = new SeckillMessage(
                activityId,
                userId,
                activity.getProductId(),
                activity.getSeckillPrice()
        );
        seckillProducer.sendSeckillMessage(msg);
        seckillMetrics.recordSeckill("accepted");
    }

    // -------------------------------------------------------------------------
    // getResult
    // -------------------------------------------------------------------------

    @Override
    public SeckillResultDTO getResult(Long activityId, Long userId) {

        String resultKey = resultKeyPrefix + activityId + ":" + userId;
        Object value = redisTemplate.opsForValue().get(resultKey);

        SeckillResultDTO dto = new SeckillResultDTO();

        if (value == null) {
            SeckillOrder order = orderService.findByActivityAndUser(activityId, userId);
            if (order != null) {
                dto.setStatus("success");
                dto.setOrderId(order.getId());
                return dto;
            }
            dto.setStatus("fail");
            return dto;
        }

        String strVal = value.toString();

        switch (strVal) {
            case "processing" -> dto.setStatus("processing");
            case "fail"       -> dto.setStatus("fail");
            default           -> {
                // 其他值即为 orderId
                dto.setStatus("success");
                dto.setOrderId(Long.parseLong(strVal));
            }
        }

        return dto;
    }
}
