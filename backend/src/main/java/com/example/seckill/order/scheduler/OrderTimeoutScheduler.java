package com.example.seckill.order.scheduler;

import com.example.seckill.common.RedisKeys;
import com.example.seckill.common.SeckillLogger;
import com.example.seckill.order.entity.SeckillOrder;
import com.example.seckill.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时取消定时任务。
 *
 * <p>每分钟扫描 Redis ZSet（{@value RedisKeys#ORDER_TIMEOUT_KEY}），
 * 查找 score ≤ 当前时间戳的过期订单，执行取消并回补库存。
 *
 * <h3>流程</h3>
 * <ol>
 *   <li>{@code ZRANGEBYSCORE} 获取 score ≤ now 的 orderId</li>
 *   <li>更新订单状态为 2（已取消）</li>
 *   <li>回补 Redis 库存（HINCRBY）</li>
 *   <li>更新前端 resultKey 为 "timeout_cancelled"</li>
 *   <li>{@code ZREM} 从超时集合移除</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final OrderService orderService;

    /**
     * 每分钟执行一次超时扫描。
     */
    @Scheduled(fixedDelay = 60_000)
    public void cancelTimeoutOrders() {
        long now = Instant.now().toEpochMilli();

        // ZRANGEBYSCORE: 获取所有 score ≤ now 的 orderId
        Set<String> expiredIds = stringRedisTemplate.opsForZSet()
                .rangeByScore(RedisKeys.ORDER_TIMEOUT_KEY, 0, now);

        if (expiredIds == null || expiredIds.isEmpty()) {
            return;
        }

        for (String orderIdStr : expiredIds) {
            Long orderId;
            try {
                orderId = Long.valueOf(orderIdStr);
            } catch (NumberFormatException e) {
                log.warn("[ORDER-CANCEL] 无效的 orderId：{}", orderIdStr);
                stringRedisTemplate.opsForZSet().remove(RedisKeys.ORDER_TIMEOUT_KEY, orderIdStr);
                continue;
            }

            try {
                // 1. 查订单（通过 ID 直接查询）
                SeckillOrder order = orderService.findOrderById(orderId);
                if (order == null) {
                    log.warn("[ORDER-CANCEL] 订单 {} 不存在，从超时集合移除", orderId);
                    stringRedisTemplate.opsForZSet().remove(RedisKeys.ORDER_TIMEOUT_KEY, orderIdStr);
                    continue;
                }

                // 2. 只取消待支付订单（status=0）
                if (order.getStatus() != null && order.getStatus() != 0) {
                    // 订单已处理（已支付或已取消），从超时集合移除
                    stringRedisTemplate.opsForZSet().remove(RedisKeys.ORDER_TIMEOUT_KEY, orderIdStr);
                    continue;
                }

                // 3. 更新订单状态为已取消
                order.setStatus(2);
                order.setUpdatedAt(LocalDateTime.now());
                orderService.updateOrder(order);

                // 4. 回补 Redis 库存（+1）
                String stockKey = RedisKeys.stockKey(order.getActivityId());
                Long newStock = stringRedisTemplate.opsForValue().increment(stockKey, 1);

                // 5. 更新前端 result key
                String resultKey = RedisKeys.resultKey(order.getActivityId(), order.getUserId());
                stringRedisTemplate.opsForValue().set(resultKey, "timeout_cancelled",
                        RedisKeys.RESULT_TTL_SECONDS, TimeUnit.SECONDS);

                // 6. 从超时 ZSet 移除
                stringRedisTemplate.opsForZSet().remove(RedisKeys.ORDER_TIMEOUT_KEY, orderIdStr);

                SeckillLogger.orderCancel(orderId, order.getActivityId(), order.getUserId());
                SeckillLogger.stockRefund(order.getActivityId(), 1,
                        newStock != null ? newStock.intValue() : -1);

            } catch (Exception e) {
                log.error("[ORDER-CANCEL] 取消失败 orderId={}, error={}", orderId, e.getMessage(), e);
            }
        }
    }

}
