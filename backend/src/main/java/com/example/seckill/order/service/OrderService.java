package com.example.seckill.order.service;

import com.example.seckill.order.entity.SeckillOrder;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface OrderService {

    /**
     * 查询当前用户的订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<SeckillOrder> listOrders(Long userId);

    /**
     * 查询单个订单（同时校验订单属于该用户）
     *
     * @param id     订单 ID
     * @param userId 当前用户 ID
     * @return 订单实体
     */
    SeckillOrder getOrder(Long id, Long userId);

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 保存后的订单实体
     */
    SeckillOrder createOrder(SeckillOrder order);

    SeckillOrder findByActivityAndUser(Long activityId, Long userId);

    /**
     * 通过订单 ID 查询（管理员用，不校验 userId）。
     *
     * @param orderId 订单 ID
     * @return 订单实体
     */
    SeckillOrder findOrderById(Long orderId);

    /**
     * 更新订单（用于超时取消等场景）。
     *
     * @param order 订单实体（必须包含 id）
     * @return 更新后的订单
     */
    SeckillOrder updateOrder(SeckillOrder order);

    /**
     * 查询所有订单（管理员使用）
     *
     * @return 全部订单列表
     */
    List<SeckillOrder> listAllOrders();

    IPage<SeckillOrder> listAllOrders(int page, int size);
}
