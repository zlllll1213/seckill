package com.example.seckill.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.mapper.OrderMapper;
import com.example.seckill.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public List<SeckillOrder> listOrders(Long userId) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getUserId, userId)
                        .orderByDesc(SeckillOrder::getCreatedAt)
        );
    }

    @Override
    public SeckillOrder getOrder(Long id, Long userId) {
        SeckillOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getId, id)
                        .eq(SeckillOrder::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return order;
    }

    @Override
    public SeckillOrder createOrder(SeckillOrder order) {
        order.setId(null);
        order.setStatus(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    @Override
    public SeckillOrder findByActivityAndUser(Long activityId, Long userId) {
        return orderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getActivityId, activityId)
                        .eq(SeckillOrder::getUserId, userId)
        );
    }

    @Override
    public SeckillOrder findOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public SeckillOrder updateOrder(SeckillOrder order) {
        if (order.getId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    @Override
    public List<SeckillOrder> listAllOrders() {
        return orderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>()
                        .orderByDesc(SeckillOrder::getCreatedAt)
        );
    }

    @Override
    public IPage<SeckillOrder> listAllOrders(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return orderMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<SeckillOrder>().orderByDesc(SeckillOrder::getCreatedAt)
        );
    }
}
