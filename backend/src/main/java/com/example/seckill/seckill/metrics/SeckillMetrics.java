package com.example.seckill.seckill.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀业务指标收集。
 */
@Component
public class SeckillMetrics {

    private final MeterRegistry registry;

    public SeckillMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 记录秒杀请求结果。
     *
     * @param result 结果标签：accepted / repeat / stock_empty / token_empty / fail
     */
    public void recordSeckill(String result) {
        Counter.builder("seckill.requests.total")
                .tag("result", result)
                .register(registry)
                .increment();
    }

    /** 订单创建延迟 */
    public void recordOrderLatency(long millis) {
        Timer.builder("seckill.order.create.latency")
                .register(registry)
                .record(millis, TimeUnit.MILLISECONDS);
    }

    /** 令牌获取延迟 */
    public void recordTokenLatency(long millis) {
        Timer.builder("seckill.token.acquire.latency")
                .register(registry)
                .record(millis, TimeUnit.MILLISECONDS);
    }

    /** Lua 脚本执行延迟 */
    public void recordLuaLatency(long millis) {
        Timer.builder("seckill.lua.execute.latency")
                .register(registry)
                .record(millis, TimeUnit.MILLISECONDS);
    }

    /** MQ 发送延迟 */
    public void recordMqSendLatency(long millis) {
        Timer.builder("seckill.mq.send.latency")
                .register(registry)
                .record(millis, TimeUnit.MILLISECONDS);
    }

    /** 订单超时取消计数 */
    public void recordOrderTimeoutCancel() {
        Counter.builder("seckill.order.timeout.cancel")
                .register(registry)
                .increment();
    }

    /** 死信消息计数 */
    public void recordDeadLetter() {
        Counter.builder("seckill.mq.deadletter")
                .register(registry)
                .increment();
    }
}
