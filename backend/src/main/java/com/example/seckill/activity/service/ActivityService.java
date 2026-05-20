package com.example.seckill.activity.service;

import com.example.seckill.activity.entity.SeckillActivity;

import java.util.List;

public interface ActivityService {

    /**
     * 查询所有秒杀活动列表
     *
     * @return 活动列表
     */
    List<SeckillActivity> listActivities();

    /**
     * 根据 ID 查询秒杀活动详情
     *
     * @param id 活动 ID
     * @return 活动实体
     */
    SeckillActivity getActivity(Long id);

    /**
     * 新增秒杀活动
     *
     * @param activity 活动信息
     * @return 保存后的活动实体
     */
    SeckillActivity createActivity(SeckillActivity activity);

    /**
     * 更新秒杀活动
     *
     * @param id       活动 ID
     * @param activity 更新内容
     * @return 更新后的活动实体
     */
    SeckillActivity updateActivity(Long id, SeckillActivity activity);
}
