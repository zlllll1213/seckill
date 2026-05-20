package com.example.seckill.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SeckillDeadLetterConsumer {

    @RabbitListener(queues = "${seckill.queue-name}.dead")
    public void onDeadLetter(SeckillMessage msg, Message message) {
        log.error("[DEAD-LETTER] 秒杀消息进入死信队列：activityId={}, userId={}, body={}",
                msg.getActivityId(),
                msg.getUserId(),
                new String(message.getBody(), StandardCharsets.UTF_8));
    }
}
