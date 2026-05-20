package com.example.seckill.mq;

import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.service.OrderService;
import com.example.seckill.seckill.metrics.SeckillMetrics;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀消息消费者，接收 MQ 消息后异步完成建单，并将结果写回 Redis 供前端轮询。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillConsumer {

    private final OrderService orderService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SeckillMetrics seckillMetrics;

    private static final long RESULT_TTL_SECONDS = 300;

    @Value("${seckill.result-key-prefix}")
    private String resultKeyPrefix;

    @Value("${seckill.stock-key-prefix}")
    private String stockKeyPrefix;

    /**
     * 监听秒杀队列，手动 ACK 模式。
     *
     * @param msg     秒杀消息体（Jackson2JsonMessageConverter 自动反序列化）
     * @param message RabbitMQ 原始消息，用于获取 deliveryTag
     * @param channel RabbitMQ Channel，用于手动 ACK/NACK
     */
    @RabbitListener(queues = "${seckill.queue-name}", ackMode = "MANUAL")
    public void onSeckillMessage(SeckillMessage msg, Message message, Channel channel)
            throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String resultKey = resultKeyPrefix + msg.getActivityId() + ":" + msg.getUserId();
        String stockKey  = stockKeyPrefix + msg.getActivityId();

        log.info("[SeckillConsumer] 收到秒杀消息：activityId={}, userId={}",
                msg.getActivityId(), msg.getUserId());

        long start = System.currentTimeMillis();
        try {
            // 1. 构造订单（status=0 代表待支付）
            SeckillOrder order = new SeckillOrder();
            order.setUserId(msg.getUserId());
            order.setActivityId(msg.getActivityId());
            order.setProductId(msg.getProductId());
            order.setPrice(msg.getSeckillPrice());
            order.setStatus(0);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // 2. 落库
            orderService.createOrder(order);

            // 3. 将结果 key 写为 orderId（前端轮询到非 "processing" 视为完成）
            redisTemplate.opsForValue().set(resultKey, order.getId().toString(),
                    RESULT_TTL_SECONDS, TimeUnit.SECONDS);

            log.info("[SeckillConsumer] 建单成功：orderId={}", order.getId());
            seckillMetrics.recordOrderLatency(System.currentTimeMillis() - start);

            // 4. 手动 ACK
            channel.basicAck(deliveryTag, false);

        } catch (DuplicateKeyException e) {
            SeckillOrder existing = orderService.findByActivityAndUser(msg.getActivityId(), msg.getUserId());
            if (existing != null) {
                redisTemplate.opsForValue().set(resultKey, existing.getId().toString(),
                        RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            }
            log.warn("[SeckillConsumer] 重复投递，订单已存在：activityId={}, userId={}",
                    msg.getActivityId(), msg.getUserId());
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[SeckillConsumer] 建单失败，activityId={}, userId={}, error={}",
                    msg.getActivityId(), msg.getUserId(), e.getMessage(), e);

            // 5. 将结果 key 设为 "fail"，通知前端
            redisTemplate.opsForValue().set(resultKey, "fail", RESULT_TTL_SECONDS, TimeUnit.SECONDS);

            // 6. 回滚库存（+1）
            redisTemplate.opsForValue().increment(stockKey);

            // 7. 拒绝消息并进入 DLQ，避免重入队死循环
            channel.basicReject(deliveryTag, false);
        }
    }
}
