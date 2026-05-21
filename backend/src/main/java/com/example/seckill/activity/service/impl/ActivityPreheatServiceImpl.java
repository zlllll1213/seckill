package com.example.seckill.activity.service.impl;

import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.mapper.ActivityMapper;
import com.example.seckill.activity.service.ActivityPreheatService;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.common.RedisKeys;
import com.example.seckill.common.SeckillLogger;
import com.example.seckill.seckill.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 活动预热服务实现。
 *
 * <p>预热流程：
 * <ol>
 *   <li>从 DB 加载活动信息</li>
 *   <li>将库存写入 Redis（覆盖旧值）</li>
 *   <li>按库存数生成秒杀令牌</li>
 *   <li>（可选）清理旧的用户标记和结果 key</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityPreheatServiceImpl implements ActivityPreheatService {

    private final ActivityMapper activityMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenService tokenService;

    @Override
    @Transactional(readOnly = true)  // 仅 DB 读取，Redis 写入不由 Spring 事务管理
    public Map<String, Object> preheat(Long activityId) {

        // 1. 从 DB 加载活动
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 写入 Redis 库存（纯 String，避免 JSON 序列化导致 Lua 解析失败）
        String stockKey = RedisKeys.stockKey(activityId);
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getStock()),
                RedisKeys.TOKEN_TTL_SECONDS, TimeUnit.SECONDS);

        // 3. 按库存生成令牌
        tokenService.generateTokens(activityId, activity.getStock());

        SeckillLogger.preheat(activityId, activity.getStock(), activity.getStock());
        log.info("[PREHEAT] 活动 {}（{}）预热完成：stock={}, tokens={}",
                activityId, activity.getName(), activity.getStock(), activity.getStock());

        // 4. 返回摘要
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("activityId", activityId);
        summary.put("name", activity.getName());
        summary.put("stock", activity.getStock());
        summary.put("tokens", tokenService.remainingTokens(activityId));
        summary.put("status", "preheated");
        return summary;
    }
}
