package com.example.seckill.activity.controller;

import com.example.seckill.activity.service.ActivityPreheatService;
import com.example.seckill.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 活动预热接口（仅管理员）。
 *
 * <p>预热会将活动信息、Redis 库存、秒杀令牌一次性加载到 Redis，
 * 确保秒杀开始前所有缓存数据就绪。
 *
 * <pre>POST /api/admin/activities/{id}/preheat</pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class ActivityPreheatController {

    private final ActivityPreheatService preheatService;

    /**
     * 预热指定活动。
     *
     * @param id 活动 ID（路径参数）
     * @return 预热结果摘要
     */
    @PostMapping("/{id}/preheat")
    public Result<Map<String, Object>> preheat(@PathVariable Long id) {
        Map<String, Object> summary = preheatService.preheat(id);
        return Result.success(summary);
    }
}
