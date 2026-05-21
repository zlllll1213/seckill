package com.example.seckill.config;

import com.example.seckill.common.SeckillLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 为每个 HTTP 请求注入 MDC traceId，贯穿整条秒杀链路。
 *
 * <p>traceId 格式：{@code REQ-<UUID 前 8 位>}，方便在日志中 grep。
 * 请求结束时自动清理 MDC。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // 尝试从请求头获取已有的 traceId（网关传递）
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = "REQ-" + UUID.randomUUID().toString().substring(0, 8);
        }
        SeckillLogger.setTraceId(traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SeckillLogger.clearTraceId();
        }
    }
}
