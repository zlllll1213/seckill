package com.example.seckill.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.seckill.common.Result;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 查询当前用户的订单列表（需登录）
     */
    @GetMapping("/api/orders")
    public Result<List<SeckillOrder>> listOrders() {
        Long userId = getCurrentUserId();
        return Result.success(orderService.listOrders(userId));
    }

    /**
     * 查询当前用户的某个订单详情（需登录，且订单必须属于该用户）
     */
    @GetMapping("/api/orders/{id}")
    public Result<SeckillOrder> getOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success(orderService.getOrder(id, userId));
    }

    /**
     * 查询所有订单（ADMIN）
     */
    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<SeckillOrder>> listAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(orderService.listAllOrders(page, size));
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
