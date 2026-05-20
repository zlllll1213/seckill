package com.example.seckill.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class AuditLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            String path = request.getRequestURI();
            if (!path.startsWith("/api/")) {
                return;
            }
            log.info("[AUDIT] user={} method={} path={} status={} costMs={} ip={}",
                    currentUser(),
                    request.getMethod(),
                    path,
                    response.getStatus(),
                    System.currentTimeMillis() - start,
                    request.getRemoteAddr());
        }
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        return principal == null ? "anonymous" : principal.toString();
    }
}
