package com.example.seckill.mq;

import com.example.seckill.common.RedisKeys;
import com.example.seckill.common.SeckillLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 死信队列消费者。
 * <p>当秒杀消息被拒绝（basicReject）且未重新入队时，消息进入死信队列。
 * 此处记录告警日志，并将 result key 标记为失败，确保前端不再一直轮询。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillDeadLetterConsumer {

    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = "${seckill.queue-name}.dead")
    public void onDeadLetter(SeckillMessage msg, Message message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        SeckillLogger.deadLetter(msg.getActivityId(), msg.getUserId(), body);

        // 确保前端能拿到失败结果，不再一直 "processing"
        String resultKey = RedisKeys.resultKey(msg.getActivityId(), msg.getUserId());
        stringRedisTemplate.opsForValue().set(resultKey, "fail",
                RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);
    }
}
