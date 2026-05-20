package com.example.seckill.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.seckill.dto.SeckillResultDTO;
import com.example.seckill.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀接口控制器。
 *
 * <p>所有路径均需登录鉴权（已在 {@code SecurityConfig} 中配置
 * {@code anyRequest().authenticated()}）。JWT 过滤器将 userId 作为
 * {@code Authentication.principal} 写入 SecurityContext，此处直接读取。
 */
@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    /**
     * 执行秒杀。
     *
     * <pre>POST /api/seckill/{activityId}</pre>
     *
     * @param activityId 秒杀活动 ID（路径参数）
     * @return 统一结果（data 为 null，code=200 表示已进入处理队列）
     */
    @PostMapping("/{activityId}")
    public Result<Void> doSeckill(@PathVariable Long activityId) {
        Long userId = getCurrentUserId();
        log.info("[SeckillController] doSeckill activityId={}, userId={}", activityId, userId);
        seckillService.doSeckill(activityId, userId);
        return Result.success();
    }

    /**
     * 轮询秒杀结果。
     *
     * <pre>GET /api/seckill/result/{activityId}</pre>
     *
     * @param activityId 秒杀活动 ID（路径参数）
     * @return 包含 status 与 orderId 的 DTO
     */
    @GetMapping("/result/{activityId}")
    public Result<SeckillResultDTO> getResult(@PathVariable Long activityId) {
        Long userId = getCurrentUserId();
        SeckillResultDTO dto = seckillService.getResult(activityId, userId);
        return Result.success(dto);
    }

    // -------------------------------------------------------------------------
    // 内部工具
    // -------------------------------------------------------------------------

    /**
     * 从 SecurityContext 中取出当前登录用户的 ID。
     * JwtAuthenticationFilter 将 userId（Long）设置为 Authentication 的 principal。
     */
    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
