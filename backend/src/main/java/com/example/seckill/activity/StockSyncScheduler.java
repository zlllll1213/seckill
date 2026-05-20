package com.example.seckill.activity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${seckill.stock-key-prefix}")
    private String stockKeyPrefix;

    @PostConstruct
    public void restoreStockFromDB() {
        List<SeckillActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>().eq(SeckillActivity::getStatus, 1));
        for (SeckillActivity activity : activities) {
            String stockKey = stockKeyPrefix + activity.getId();
            if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
                redisTemplate.opsForValue().set(stockKey, activity.getStock().toString());
                log.info("[StockRestore] 恢复活动 {} 库存到 Redis: {}", activity.getId(), activity.getStock());
            }
        }
    }

    @Scheduled(fixedRate = 30_000)
    public void syncStockToDB() {
        Set<String> keys = redisTemplate.keys(stockKeyPrefix + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            Object stock = redisTemplate.opsForValue().get(key);
            if (stock == null) {
                continue;
            }
            try {
                Long activityId = Long.parseLong(key.substring(stockKeyPrefix.length()));
                SeckillActivity activity = new SeckillActivity();
                activity.setId(activityId);
                activity.setStock(Integer.parseInt(stock.toString()));
                activityMapper.updateById(activity);
            } catch (RuntimeException e) {
                log.warn("[StockSync] 同步库存失败：key={}, stock={}", key, stock, e);
            }
        }
    }
}
