package com.example.seckill.mq;

import com.example.seckill.common.RedisKeys;
import com.example.seckill.common.SeckillLogger;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.service.OrderService;
import com.example.seckill.seckill.metrics.SeckillMetrics;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillMetrics seckillMetrics;

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
        String resultKey = RedisKeys.resultKey(msg.getActivityId(), msg.getUserId());
        String stockKey  = RedisKeys.stockKey(msg.getActivityId());

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
            stringRedisTemplate.opsForValue().set(resultKey, order.getId().toString(),
                    RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);

            // 4. 【新增】订单超时追踪：将 orderId 写入 ZSet，score 为超时时间戳（ms）
            long timeoutTs = Instant.now().toEpochMilli()
                    + RedisKeys.ORDER_TIMEOUT_MINUTES * 60 * 1000L;
            stringRedisTemplate.opsForZSet().add(RedisKeys.ORDER_TIMEOUT_KEY,
                    order.getId().toString(), (double) timeoutTs);

            long elapsedMs = System.currentTimeMillis() - start;
            SeckillLogger.mqConsumeSuccess(msg.getActivityId(), msg.getUserId(),
                    order.getId(), elapsedMs);
            seckillMetrics.recordOrderLatency(elapsedMs);

            // 5. 手动 ACK
            channel.basicAck(deliveryTag, false);

        } catch (DuplicateKeyException e) {
            // 重复消费 → 幂等处理：查已存在的订单写回 Redis
            SeckillOrder existing = orderService.findByActivityAndUser(
                    msg.getActivityId(), msg.getUserId());
            if (existing != null) {
                stringRedisTemplate.opsForValue().set(resultKey, existing.getId().toString(),
                        RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            }
            SeckillLogger.mqRepeat(msg.getActivityId(), msg.getUserId(),
                    existing != null ? existing.getId() : null);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            SeckillLogger.mqConsumeFail(msg.getActivityId(), msg.getUserId(), e.getMessage());

            // 将结果 key 设为 "fail"，通知前端
            stringRedisTemplate.opsForValue().set(resultKey, "fail",
                    RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);

            // 回滚库存（+1）
            stringRedisTemplate.opsForValue().increment(stockKey);

            // 拒绝消息并进入 DLQ，避免重入队死循环
            channel.basicReject(deliveryTag, false);
        }
    }
}
