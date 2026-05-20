package com.example.seckill.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_activity")
public class SeckillActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String name;

    private BigDecimal seckillPrice;

    private Integer stock;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 活动状态：0-待开始，1-进行中，2-已结束
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
