package com.example.seckill.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.mapper.ActivityMapper;
import com.example.seckill.activity.service.ActivityService;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.common.HtmlSanitizer;
import com.example.seckill.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<SeckillActivity> listActivities() {
        return activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreatedAt)
        );
    }

    @Override
    public SeckillActivity getActivity(Long id) {
        SeckillActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        if (activity.getStatus() == 1) {
            String stock = stringRedisTemplate.opsForValue().get(RedisKeys.stockKey(id));
            if (stock != null) {
                activity.setStock(Integer.parseInt(stock));
            }
        }
        return activity;
    }

    @Override
    public SeckillActivity createActivity(SeckillActivity activity) {
        sanitize(activity);
        activity.setId(null);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.insert(activity);

        // 保存成功后，将库存同步初始化到 Redis（纯 String，避免 JSON 序列化导致 Lua 解析失败）
        String stockKey = RedisKeys.stockKey(activity.getId());
        stringRedisTemplate.opsForValue().set(stockKey, activity.getStock().toString());
        log.info("[ActivityService] 初始化 Redis 库存：key={}, stock={}", stockKey, activity.getStock());

        return activity;
    }

    @Override
    public SeckillActivity updateActivity(Long id, SeckillActivity activity) {
        SeckillActivity existing = activityMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        sanitize(activity);
        if (existing.getStatus() == 1) {
            activity.setSeckillPrice(existing.getSeckillPrice());
            activity.setStock(existing.getStock());
        }
        activity.setId(id);
        activity.setCreatedAt(existing.getCreatedAt());
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);
        return activity;
    }

    private void sanitize(SeckillActivity activity) {
        if (!StringUtils.hasText(activity.getName())
                || activity.getProductId() == null
                || activity.getSeckillPrice() == null
                || activity.getSeckillPrice().signum() < 0
                || activity.getStock() == null
                || activity.getStock() < 1
                || activity.getStartTime() == null
                || activity.getEndTime() == null
                || !activity.getEndTime().isAfter(activity.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        activity.setName(HtmlSanitizer.sanitize(activity.getName()));
    }
}
