package com.example.seckill.activity.service;

import java.util.Map;

/**
 * 活动预热服务接口。
 */
public interface ActivityPreheatService {

    /**
     * 预热指定活动：加载活动信息、Redis 库存、秒杀令牌。
     *
     * @param activityId 活动 ID
     * @return 预热结果摘要（活动名、库存数、Token 数等）
     */
    Map<String, Object> preheat(Long activityId);
}
