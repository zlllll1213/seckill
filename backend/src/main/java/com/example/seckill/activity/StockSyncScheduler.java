package com.example.seckill.activity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.mapper.ActivityMapper;
import com.example.seckill.common.RedisKeys;
import com.example.seckill.seckill.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockSyncScheduler {

    private final ActivityMapper activityMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenService tokenService;

    @PostConstruct
    public void restoreStockFromDB() {
        List<SeckillActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>().eq(SeckillActivity::getStatus, 1));
        for (SeckillActivity activity : activities) {
            String stockKey = RedisKeys.stockKey(activity.getId());
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(stockKey))) {
                stringRedisTemplate.opsForValue().set(stockKey, activity.getStock().toString());
                log.info("[StockRestore] 恢复活动 {} 库存到 Redis: {}", activity.getId(), activity.getStock());
            }
            // 同时恢复令牌（如果令牌 key 不存在）
            String tokenKey = RedisKeys.tokenKey(activity.getId());
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(tokenKey))) {
                tokenService.generateTokens(activity.getId(), activity.getStock());
                log.info("[StockRestore] 恢复活动 {} 令牌 {}", activity.getId(), activity.getStock());
            }
        }
    }

    @Scheduled(fixedRate = 30_000)
    public void syncStockToDB() {
        Set<String> keys = stringRedisTemplate.keys(RedisKeys.STOCK_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            String stock = stringRedisTemplate.opsForValue().get(key);
            if (stock == null) {
                continue;
            }
            try {
                Long activityId = Long.parseLong(key.substring(RedisKeys.STOCK_PREFIX.length()));
                SeckillActivity activity = new SeckillActivity();
                activity.setId(activityId);
                activity.setStock(Integer.parseInt(stock));
                activityMapper.updateById(activity);
            } catch (RuntimeException e) {
                log.warn("[StockSync] 同步库存失败：key={}, stock={}", key, stock, e);
            }
        }
    }
}
