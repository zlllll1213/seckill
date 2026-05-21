package com.example.seckill.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * 秒杀核心链路统一日志工具。
 *
 * <p>所有秒杀链路日志通过本类输出，统一格式为：
 * <pre>{@code [TAG] activityId=xxx, userId=xxx, ...}</pre>
 *
 * <p>TAG 清单：
 * <ul>
 *   <li>{@code [SECKILL]}    — 秒杀请求入口</li>
 *   <li>{@code [TOKEN]}      — 令牌获取</li>
 *   <li>{@code [LUA]}        — Lua 脚本执行</li>
 *   <li>{@code [MQ-SEND]}    — MQ 消息发送</li>
 *   <li>{@code [MQ-CONFIRM]} — Publisher Confirm 回调</li>
 *   <li>{@code [MQ-RETURN]}  — Returns Callback（路由失败）</li>
 *   <li>{@code [MQ-CONSUME]} — MQ 消息消费</li>
 *   <li>{@code [MQ-FALLBACK]}</li> — MQ 兜底重投</li>
 *   <li>{@code [REPEAT]}     — 重复消费（幂等处理）</li>
 *   <li>{@code [DEAD-LETTER]}</li> — 死信消息</li>
 *   <li>{@code [ORDER-CANCEL]}</li> — 订单超时取消</li>
 *   <li>{@code [STOCK-REFUND]}</li> — 库存回补</li>
 *   <li>{@code [PREHEAT]}    — 活动预热</li>
 * </ul>
 */
@Slf4j
public final class SeckillLogger {

    private SeckillLogger() {}

    // -----------------------------------------------------------------------
    // 链路节点日志
    // -----------------------------------------------------------------------

    /** 秒杀请求入口 */
    public static void seckillReq(Long activityId, Long userId) {
        log.info("[SECKILL] activityId={}, userId={}", activityId, userId);
    }

    /** 令牌获取成功 */
    public static void tokenAcquired(Long activityId, Long userId, long remaining) {
        log.info("[TOKEN] activityId={}, userId={}, remainingTokens={}", activityId, userId, remaining);
    }

    /** 令牌获取失败（已耗尽） */
    public static void tokenEmpty(Long activityId, Long userId) {
        log.warn("[TOKEN] activityId={}, userId={}, result=EMPTY", activityId, userId);
    }

    /** Lua 脚本执行 */
    public static void luaResult(Long activityId, Long userId, long result, long elapsedMs) {
        String desc = switch ((int) result) {
            case 0 -> "SUCCESS";
            case 1 -> "REPEAT";
            case 2 -> "STOCK_EMPTY";
            default -> "UNKNOWN(" + result + ")";
        };
        log.info("[LUA] activityId={}, userId={}, result={}, elapsedMs={}",
                activityId, userId, desc, elapsedMs);
    }

    /** MQ 消息发送 */
    public static void mqSend(Long activityId, Long userId) {
        log.info("[MQ-SEND] activityId={}, userId={}", activityId, userId);
    }

    /** MQ Publisher Confirm 成功 */
    public static void mqConfirmAck(String correlationId) {
        log.debug("[MQ-CONFIRM] correlationId={}, ack=true", correlationId);
    }

    /** MQ Publisher Confirm 失败 */
    public static void mqConfirmNack(String correlationId, String cause) {
        log.error("[MQ-CONFIRM] correlationId={}, ack=false, cause={}", correlationId, cause);
    }

    /** MQ Returns Callback（消息路由失败） */
    public static void mqReturn(int replyCode, String replyText, String exchange, String routingKey) {
        log.error("[MQ-RETURN] replyCode={}, replyText={}, exchange={}, routingKey={}",
                replyCode, replyText, exchange, routingKey);
    }

    /** MQ 消费成功建单 */
    public static void mqConsumeSuccess(Long activityId, Long userId, Long orderId, long elapsedMs) {
        log.info("[MQ-CONSUME] activityId={}, userId={}, orderId={}, elapsedMs={}",
                activityId, userId, orderId, elapsedMs);
    }

    /** MQ 消费失败 */
    public static void mqConsumeFail(Long activityId, Long userId, String error) {
        log.error("[MQ-CONSUME] activityId={}, userId={}, result=FAIL, error={}",
                activityId, userId, error);
    }

    /** MQ 重复消费（幂等处理） */
    public static void mqRepeat(Long activityId, Long userId, Long orderId) {
        log.warn("[REPEAT] activityId={}, userId={}, orderId={}, action=ACK",
                activityId, userId, orderId);
    }

    /** 死信消息 */
    public static void deadLetter(Long activityId, Long userId, String body) {
        log.error("[DEAD-LETTER] activityId={}, userId={}, body={}", activityId, userId, body);
    }

    /** MQ fallback 重投 */
    public static void mqFallbackRetry(Long activityId, Long userId, String key) {
        log.warn("[MQ-FALLBACK] activityId={}, userId={}, retryKey={}", activityId, userId, key);
    }

    /** 订单超时取消 */
    public static void orderCancel(Long orderId, Long activityId, Long userId) {
        log.info("[ORDER-CANCEL] orderId={}, activityId={}, userId={}", orderId, activityId, userId);
    }

    /** 库存回补 */
    public static void stockRefund(Long activityId, int refundCount, int currentStock) {
        log.info("[STOCK-REFUND] activityId={}, refundAmount={}, currentStock={}",
                activityId, refundCount, currentStock);
    }

    /** 活动预热 */
    public static void preheat(Long activityId, int stock, int tokenCount) {
        log.info("[PREHEAT] activityId={}, stock={}, tokenCount={}", activityId, stock, tokenCount);
    }

    // -----------------------------------------------------------------------
    // MDC traceId 支持
    // -----------------------------------------------------------------------

    public static final String MDC_TRACE_ID = "traceId";

    /** 设置当前请求的 traceId（通常在 Filter 中调用） */
    public static void setTraceId(String traceId) {
        MDC.put(MDC_TRACE_ID, traceId);
    }

    /** 清理 MDC（请求结束时调用） */
    public static void clearTraceId() {
        MDC.remove(MDC_TRACE_ID);
    }
}
