package com.example.seckill.config;

import com.example.seckill.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final Map<String, FixedWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        LimitRule rule = resolveRule(request);
        if (rule == null) {
            chain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + ":" + userKey(request) + ":" + rule.name();
        FixedWindow window = windows.computeIfAbsent(key, ignored -> new FixedWindow(rule.window()));
        if (!window.tryAcquire(rule.limit())) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.fail(429, "请求过于频繁，请稍后再试")));
            return;
        }

        chain.doFilter(request, response);
    }

    private LimitRule resolveRule(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            return new LimitRule("login", 10, Duration.ofSeconds(10));
        }
        if ("POST".equals(method) && "/api/auth/register".equals(path)) {
            return new LimitRule("register", 5, Duration.ofSeconds(10));
        }
        if ("POST".equals(method) && path.matches("/api/seckill/\\d+")) {
            return new LimitRule("seckill", 1, Duration.ofSeconds(1));
        }
        if ("GET".equals(method) && path.matches("/api/seckill/result/\\d+")) {
            return new LimitRule("seckill-result", 5, Duration.ofSeconds(1));
        }
        return null;
    }

    private String userKey(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId == null ? "anonymous" : userId.toString();
    }

    private record LimitRule(String name, int limit, Duration window) {
    }

    private static final class FixedWindow {
        private final long windowMillis;
        private long windowStart;
        private int count;

        private FixedWindow(Duration window) {
            this.windowMillis = window.toMillis();
            this.windowStart = System.currentTimeMillis();
        }

        private synchronized boolean tryAcquire(int limit) {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowMillis) {
                windowStart = now;
                count = 0;
            }
            if (count >= limit) {
                return false;
            }
            count++;
            return true;
        }
    }
}
