package com.example.seckill.mq;

import com.example.seckill.common.SeckillLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 秒杀消息生产者，将秒杀请求投递到 RabbitMQ，由消费者异步完成建单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillProducer {

    private final RabbitTemplate rabbitTemplate;
    private final SeckillMqFallbackService fallbackService;

    @Value("${seckill.exchange-name}")
    private String exchangeName;

    @Value("${seckill.routing-key}")
    private String routingKey;

    /**
     * 发送秒杀消息到 MQ。
     *
     * @param msg 秒杀消息体
     */
    public void sendSeckillMessage(SeckillMessage msg) {
        fallbackService.recordPending(msg);
        doSend(msg);
    }

    private void doSend(SeckillMessage msg) {
        SeckillLogger.mqSend(msg.getActivityId(), msg.getUserId());
        CorrelationData correlationData = new CorrelationData(msg.getActivityId() + ":" + msg.getUserId());
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, msg, correlationData);
        } catch (AmqpException e) {
            log.error("[MQ-SEND] 秒杀消息投递异常，已保留 fallback：activityId={}, userId={}",
                    msg.getActivityId(), msg.getUserId(), e);
            SeckillLogger.mqConfirmNack(
                    msg.getActivityId() + ":" + msg.getUserId(), e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 30_000)
    public void retryFallbackMessages() {
        Set<String> keys = fallbackService.keys();
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            SeckillMessage msg = fallbackService.get(key);
            if (msg == null) {
                fallbackService.delete(key);
                continue;
            }
            SeckillLogger.mqFallbackRetry(msg.getActivityId(), msg.getUserId(), key);
            doSend(msg);
        }
    }
}
