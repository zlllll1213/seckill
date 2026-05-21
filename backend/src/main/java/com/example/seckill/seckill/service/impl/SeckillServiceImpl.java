package com.example.seckill.seckill.service.impl;

import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.service.ActivityService;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.common.RedisKeys;
import com.example.seckill.common.SeckillLogger;
import com.example.seckill.mq.SeckillMessage;
import com.example.seckill.mq.SeckillProducer;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.service.OrderService;
import com.example.seckill.seckill.dto.SeckillResultDTO;
import com.example.seckill.seckill.metrics.SeckillMetrics;
import com.example.seckill.seckill.service.SeckillService;
import com.example.seckill.seckill.service.TokenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final ActivityService activityService;
    private final SeckillProducer seckillProducer;
    private final OrderService orderService;
    private final SeckillMetrics seckillMetrics;
    private final TokenService tokenService;

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

        long t0 = System.currentTimeMillis();

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

        // 3. 【新增】令牌预检：SPOP 原子获取令牌，无令牌则快速失败
        if (!tokenService.tryAcquireToken(activityId, userId)) {
            seckillMetrics.recordSeckill("token_empty");
            throw new BusinessException(ErrorCode.SECKILL_TOKEN_EMPTY);
        }

        // 4. 拼装 Redis key（使用统一 RedisKeys）
        String stockKey    = RedisKeys.stockKey(activityId);
        String userMarkKey = RedisKeys.userMarkKey(activityId);
        String resultKey   = RedisKeys.resultKey(activityId, userId);

        // 5. 执行 Lua 脚本（原子：判重 → 判库存 → 扣减 → 记用户）
        long luaStart = System.currentTimeMillis();
        // 必须用 StringRedisTemplate 执行 Lua，否则 JSON 序列化后的带引号字符串
        // 会导致 Lua tonumber 解析失败（如 "\"100\"" → nil）
        Long luaResult = stringRedisTemplate.execute(
                stockDeductScript,
                Arrays.asList(stockKey, userMarkKey),
                userId.toString()
        );
        long luaElapsed = System.currentTimeMillis() - luaStart;

        SeckillLogger.luaResult(activityId, userId,
                luaResult != null ? luaResult : -1, luaElapsed);

        if (luaResult == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        // 6. 根据 Lua 返回值分支处理
        if (luaResult == 1L) {
            seckillMetrics.recordSeckill("repeat");
            throw new BusinessException(ErrorCode.SECKILL_REPEAT);
        }
        if (luaResult == 2L) {
            seckillMetrics.recordSeckill("stock_empty");
            throw new BusinessException(ErrorCode.SECKILL_STOCK_EMPTY);
        }

        // luaResult == 0：秒杀成功
        // 7. 写 result key 为 "processing"，前端轮询使用
        stringRedisTemplate.opsForValue().set(resultKey, "processing",
                RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);

        // 8. 投递 MQ 异步建单
        SeckillMessage msg = new SeckillMessage(
                activityId,
                userId,
                activity.getProductId(),
                activity.getSeckillPrice()
        );
        seckillProducer.sendSeckillMessage(msg);
        seckillMetrics.recordSeckill("accepted");

        long totalElapsed = System.currentTimeMillis() - t0;
        SeckillLogger.seckillReq(activityId, userId);
        log.info("[SECKILL] activityId={}, userId={}, totalElapsedMs={}", activityId, userId, totalElapsed);
    }

    // -------------------------------------------------------------------------
    // getResult
    // -------------------------------------------------------------------------

    @Override
    public SeckillResultDTO getResult(Long activityId, Long userId) {

        String resultKey = RedisKeys.resultKey(activityId, userId);
        String strVal = stringRedisTemplate.opsForValue().get(resultKey);

        SeckillResultDTO dto = new SeckillResultDTO();

        // 【增强】Redis resultKey 不存在 → 兜底查 DB
        if (strVal == null) {
            SeckillOrder order = orderService.findByActivityAndUser(activityId, userId);
            if (order != null) {
                // 订单已创建但 resultKey 可能已过期 → 补写 Redis 并返回
                dto.setStatus(order.getStatus() != null && order.getStatus() == 2
                        ? "timeout_cancelled" : "success");
                dto.setOrderId(order.getId());

                // 补写 result key（避免下次再查 DB）
                stringRedisTemplate.opsForValue().set(resultKey, order.getId().toString(),
                        RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);
                log.info("[RESULT-FALLBACK] activityId={}, userId={}, orderId={} 从DB兜底查询补写Redis",
                        activityId, userId, order.getId());
                return dto;
            }
            dto.setStatus("fail");
            return dto;
        }

        switch (strVal) {
            case "processing" -> dto.setStatus("processing");
            case "fail"       -> dto.setStatus("fail");
            case "timeout_cancelled" -> dto.setStatus("timeout_cancelled");
            default           -> {
                // 其他值即为 orderId
                dto.setStatus("success");
                try {
                    dto.setOrderId(Long.parseLong(strVal));
                } catch (NumberFormatException ignored) {
                    dto.setStatus("fail");
                }
            }
        }

        return dto;
    }
}
