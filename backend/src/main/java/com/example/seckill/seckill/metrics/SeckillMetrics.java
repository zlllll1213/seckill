package com.example.seckill.seckill.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SeckillMetrics {

    private final MeterRegistry registry;

    public SeckillMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSeckill(String result) {
        Counter.builder("seckill.requests.total")
                .tag("result", result)
                .register(registry)
                .increment();
    }

    public void recordOrderLatency(long millis) {
        Timer.builder("seckill.order.create.latency")
                .register(registry)
                .record(millis, TimeUnit.MILLISECONDS);
    }
}
