package com.example.seckill.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_DUPLICATE(1002, "用户名已被占用"),
    PASSWORD_ERROR(1003, "密码错误"),

    // 商品相关
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_OFF_SHELF(2002, "商品已下架"),

    // 秒杀相关
    ACTIVITY_NOT_FOUND(3001, "秒杀活动不存在"),
    ACTIVITY_NOT_STARTED(3002, "秒杀活动尚未开始"),
    ACTIVITY_ENDED(3003, "秒杀活动已结束"),
    SECKILL_STOCK_EMPTY(3004, "库存不足"),
    SECKILL_REPEAT(3005, "您已参与过该秒杀活动"),
    SECKILL_PROCESSING(3006, "秒杀处理中，请稍候"),
    SECKILL_TOKEN_EMPTY(3007, "秒杀令牌已耗尽，请稍后再试"),
    SECKILL_PREHEAT_REQUIRED(3008, "活动尚未预热，请联系管理员"),

    // 订单相关
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态异常"),
    ORDER_TIMEOUT_CANCELLED(4003, "订单已超时取消");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
