package com.example.seckill.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long activityId;

    private Long productId;

    private BigDecimal price;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
