package com.example.seckill.activity.controller;

import com.example.seckill.activity.entity.SeckillActivity;
import com.example.seckill.activity.service.ActivityService;
import com.example.seckill.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    // ========== 公开接口 ==========

    /**
     * 查询所有秒杀活动（公开）
     */
    @GetMapping("/api/activities")
    public Result<List<SeckillActivity>> listActivities() {
        return Result.success(activityService.listActivities());
    }

    /**
     * 查询秒杀活动详情（公开）
     */
    @GetMapping("/api/activities/{id}")
    public Result<SeckillActivity> getActivity(@PathVariable Long id) {
        return Result.success(activityService.getActivity(id));
    }

    // ========== 管理员接口 ==========

    /**
     * 新增秒杀活动（ADMIN）
     */
    @PostMapping("/api/admin/activities")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SeckillActivity> createActivity(@RequestBody SeckillActivity activity) {
        return Result.success(activityService.createActivity(activity));
    }

    /**
     * 更新秒杀活动（ADMIN）
     */
    @PutMapping("/api/admin/activities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SeckillActivity> updateActivity(@PathVariable Long id,
                                                   @RequestBody SeckillActivity activity) {
        return Result.success(activityService.updateActivity(id, activity));
    }
}
