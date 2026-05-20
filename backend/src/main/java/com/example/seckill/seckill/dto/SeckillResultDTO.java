package com.example.seckill.seckill.dto;

import lombok.Data;

/**
 * 秒杀结果 DTO，供前端轮询接口使用。
 * <ul>
 *   <li>status = "processing" — 秒杀请求处理中</li>
 *   <li>status = "success"    — 秒杀成功，orderId 不为空</li>
 *   <li>status = "fail"       — 秒杀失败（建单异常或库存回滚）</li>
 * </ul>
 */
@Data
public class SeckillResultDTO {

    /** 处理状态："processing" / "success" / "fail" */
    private String status;

    /** 成功时的订单 ID，其余状态为 null */
    private Long orderId;
}
