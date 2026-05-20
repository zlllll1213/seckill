package com.example.seckill.admin;

import com.example.seckill.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DashboardStats> getStats() {
        return Result.success(adminStatsService.getStats());
    }
}
