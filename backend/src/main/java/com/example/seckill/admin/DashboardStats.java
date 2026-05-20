package com.example.seckill.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStats {
    private long totalOrders;
    private long totalProducts;
    private long activeActivities;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
}
