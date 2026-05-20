package com.example.seckill.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeckillMqFallbackService {

    private static final String PREFIX = "seckill:fallback:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void recordPending(SeckillMessage msg) {
        redisTemplate.opsForValue().set(key(msg.getActivityId(), msg.getUserId()), msg, 10, TimeUnit.MINUTES);
    }

    public void markDelivered(String correlationId) {
        redisTemplate.delete(PREFIX + correlationId);
    }

    public Set<String> keys() {
        return redisTemplate.keys(PREFIX + "*");
    }

    public SeckillMessage get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof SeckillMessage msg ? msg : null;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    private String key(Long activityId, Long userId) {
        return PREFIX + activityId + ":" + userId;
    }
}
