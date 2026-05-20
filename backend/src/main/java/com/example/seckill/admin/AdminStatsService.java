package com.example.seckill.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.mapper.ActivityMapper;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.mapper.OrderMapper;
import com.example.seckill.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ActivityMapper activityMapper;

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        List<SeckillOrder> paidOrders = orderMapper.selectList(
                new LambdaQueryWrapper<SeckillOrder>().eq(SeckillOrder::getStatus, 1));
        stats.setTotalOrders(orderMapper.selectCount(null));
        stats.setTotalProducts(productMapper.selectCount(null));
        stats.setActiveActivities(activityMapper.selectCount(
                new LambdaQueryWrapper<SeckillActivity>().eq(SeckillActivity::getStatus, 1)));
        stats.setTotalRevenue(paidOrders.stream()
                .map(SeckillOrder::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return stats;
    }
}
