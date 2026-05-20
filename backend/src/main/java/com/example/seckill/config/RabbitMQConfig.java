package com.example.seckill.config;

import com.example.seckill.mq.SeckillMqFallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final SeckillMqFallbackService fallbackService;

    @Value("${seckill.queue-name}")
    private String queueName;

    @Value("${seckill.exchange-name}")
    private String exchangeName;

    @Value("${seckill.routing-key}")
    private String routingKey;

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", routingKey + ".dead")
                .build();
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange seckillDeadLetterExchange() {
        return new DirectExchange(exchangeName + ".dlx", true, false);
    }

    @Bean
    public Queue seckillDeadLetterQueue() {
        return QueueBuilder.durable(queueName + ".dead").build();
    }

    @Bean
    public Binding seckillBinding(Queue seckillQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with(routingKey);
    }

    @Bean
    public Binding seckillDeadLetterBinding(Queue seckillDeadLetterQueue,
                                            DirectExchange seckillDeadLetterExchange) {
        return BindingBuilder.bind(seckillDeadLetterQueue)
                .to(seckillDeadLetterExchange)
                .with(routingKey + ".dead");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            String id = correlationData == null ? null : correlationData.getId();
            if (ack) {
                if (id != null) {
                    fallbackService.markDelivered(id);
                }
                return;
            }
            log.error("[MQ] 消息发送未确认：correlationId={}, cause={}", id, cause);
        });
        template.setReturnsCallback(returned -> log.error(
                "[MQ] 消息路由失败：replyCode={}, replyText={}, exchange={}, routingKey={}",
                returned.getReplyCode(),
                returned.getReplyText(),
                returned.getExchange(),
                returned.getRoutingKey()));
        return template;
    }
}
