package com.example.seckill.common;

/**
 * Redis Key 集中管理类，避免魔法字符串散落各处。
 *
 * <h3>Key 命名规范</h3>
 * <ul>
 *   <li>{@code seckill:stock:<activityId>} — 活动库存（String，Lua 原子操作）</li>
 *   <li>{@code seckill:user:<activityId>}  — 用户标记（Set，防重复秒杀）</li>
 *   <li>{@code seckill:result:<activityId>:<userId>} — 秒杀结果（String，前端轮询）</li>
 *   <li>{@code seckill:token:<activityId>}  — 秒杀令牌池（Set，SPOP 原子获取）</li>
 *   <li>{@code seckill:fallback:<activityId>:<userId>} — MQ 发送 fallback</li>
 *   <li>{@code seckill:order:timeout}   — 订单超时 ZSet（score=超时时间戳，member=orderId）</li>
 * </ul>
 */
public final class RedisKeys {

    private RedisKeys() {
        // 工具类不可实例化
    }

    /** 活动库存（String，Lua 原子扣减） */
    public static final String STOCK_PREFIX = "seckill:stock:";

    /** 用户已参与标记（Set） */
    public static final String USER_MARK_PREFIX = "seckill:user:";

    /** 秒杀结果（String，前端轮询） */
    public static final String RESULT_PREFIX = "seckill:result:";

    /** 秒杀令牌池（Set，SPOP 获取） */
    public static final String TOKEN_PREFIX = "seckill:token:";

    /** 订单超时记录（ZSet，score 为超时时间戳 epoch ms） */
    public static final String ORDER_TIMEOUT_KEY = "seckill:order:timeout";

    /** MQ fallback 前缀 */
    public static final String FALLBACK_PREFIX = "seckill:fallback:";

    // -----------------------------------------------------------------------
    // 便捷构造方法
    // -----------------------------------------------------------------------

    public static String stockKey(Long activityId) {
        return STOCK_PREFIX + activityId;
    }

    public static String userMarkKey(Long activityId) {
        return USER_MARK_PREFIX + activityId;
    }

    public static String resultKey(Long activityId, Long userId) {
        return RESULT_PREFIX + activityId + ":" + userId;
    }

    /** 构建 resultKey pattern（activityId:*） */
    public static String resultPattern(Long activityId) {
        return RESULT_PREFIX + activityId + ":*";
    }

    public static String tokenKey(Long activityId) {
        return TOKEN_PREFIX + activityId;
    }

    public static String fallbackKey(Long activityId, Long userId) {
        return FALLBACK_PREFIX + activityId + ":" + userId;
    }

    /** 默认 TTL（秒） */
    public static final long RESULT_TTL_SECONDS = 300;
    public static final long FALLBACK_TTL_SECONDS = 600;
    public static final long TOKEN_TTL_SECONDS = 3600;

    /** 订单超时默认时间（分钟） */
    public static final long ORDER_TIMEOUT_MINUTES = 15;
}
