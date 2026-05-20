package com.example.seckill.seckill.service;

import com.example.seckill.seckill.dto.SeckillResultDTO;

/**
 * 秒杀核心服务接口。
 */
public interface SeckillService {

    /**
     * 执行秒杀逻辑：校验活动 → Lua 原子扣减库存 → 发 MQ 异步建单。
     *
     * @param activityId 秒杀活动 ID
     * @param userId     当前登录用户 ID
     */
    void doSeckill(Long activityId, Long userId);

    /**
     * 查询秒杀结果（前端轮询使用）。
     *
     * @param activityId 秒杀活动 ID
     * @param userId     当前登录用户 ID
     * @return 秒杀结果 DTO
     */
    SeckillResultDTO getResult(Long activityId, Long userId);
}
