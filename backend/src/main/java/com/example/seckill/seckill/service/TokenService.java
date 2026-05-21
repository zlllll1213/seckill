package com.example.seckill.seckill.service;

import com.example.seckill.common.RedisKeys;
import com.example.seckill.common.SeckillLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀令牌服务。
 *
 * <p>核心思路：为每个秒杀活动预生成与库存等量的令牌存入 Redis Set。
 * 秒杀请求到达时通过 {@code SPOP} 原子获取一枚令牌，若返回 null 则
 * 令牌已耗尽，请求直接快速失败，无需执行后续的 Lua 脚本。
 *
 * <h3>令牌生命周期</h3>
 * <ol>
 *   <li>活动预热 → {@link #generateTokens(Long, int)} 批量 SADD</li>
 *   <li>秒杀请求 → {@link #tryAcquireToken(Long)} SPOP 原子获取</li>
 *   <li>活动结束 → 令牌自然过期（TTL），或手动清理</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 为指定活动生成令牌。
     * <p>会先删除旧的令牌 key，再重新生成。
     *
     * @param activityId 活动 ID
     * @param count      令牌数量（通常与活动库存一致）
     */
    public void generateTokens(Long activityId, int count) {
        String tokenKey = RedisKeys.tokenKey(activityId);

        // 清理旧令牌
        stringRedisTemplate.delete(tokenKey);

        if (count <= 0) {
            log.warn("[TOKEN] 活动 {} 令牌数量为 0，无令牌生成", activityId);
            return;
        }

        // 批量 SADD
        String[] tokens = new String[count];
        for (int i = 0; i < count; i++) {
            tokens[i] = String.valueOf(i + 1);  // token 值 = 1..count，仅用于占位
        }
        stringRedisTemplate.opsForSet().add(tokenKey, tokens);

        // 设置过期时间（活动结束后令牌自然失效）
        stringRedisTemplate.expire(tokenKey, RedisKeys.TOKEN_TTL_SECONDS, TimeUnit.SECONDS);

        SeckillLogger.preheat(activityId, count, count);
        log.info("[TOKEN] 活动 {} 生成 {} 枚令牌", activityId, count);
    }

    /**
     * 尝试获取一枚令牌（SPOP，原子操作）。
     *
     * @param activityId 活动 ID
     * @param userId     用户 ID（日志用）
     * @return {@code true} 获取成功，{@code false} 令牌已耗尽
     */
    public boolean tryAcquireToken(Long activityId, Long userId) {
        String tokenKey = RedisKeys.tokenKey(activityId);

        // SPOP：原子弹出 set 中的一个元素，若 set 为空返回 null
        String token = stringRedisTemplate.opsForSet().pop(tokenKey);
        if (token == null) {
            SeckillLogger.tokenEmpty(activityId, userId);
            return false;
        }

        Long remaining = stringRedisTemplate.opsForSet().size(tokenKey);
        SeckillLogger.tokenAcquired(activityId, userId, remaining != null ? remaining : 0);
        return true;
    }

    /**
     * 查看令牌剩余数量。
     *
     * @param activityId 活动 ID
     * @return 剩余令牌数，key 不存在返回 0
     */
    public long remainingTokens(Long activityId) {
        Long size = stringRedisTemplate.opsForSet().size(RedisKeys.tokenKey(activityId));
        return size != null ? size : 0;
    }

    /**
     * 清理活动的令牌。
     *
     * @param activityId 活动 ID
     */
    public void clearTokens(Long activityId) {
        stringRedisTemplate.delete(RedisKeys.tokenKey(activityId));
        log.info("[TOKEN] 活动 {} 令牌已清理", activityId);
    }
}
