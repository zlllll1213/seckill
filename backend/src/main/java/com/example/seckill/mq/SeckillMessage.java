package com.example.seckill.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀消息体，用于 RabbitMQ 异步传递秒杀请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 秒杀活动 ID */
    private Long activityId;

    /** 下单用户 ID */
    private Long userId;

    /** 商品 ID */
    private Long productId;

    /** 秒杀价格（快照，防止活动价格变更后订单价格不一致） */
    private BigDecimal seckillPrice;
}
