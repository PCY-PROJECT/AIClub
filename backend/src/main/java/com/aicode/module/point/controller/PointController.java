package com.aicode.module.point.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.point.entity.PointRecord;
import com.aicode.module.point.service.PointService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /** 积分流水列表（分页） */
    @GetMapping("/records")
    public R<Page<PointRecord>> records(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(pointService.getRecords(SecurityUtil.getCurrentUserId(), page, size));
    }

    /** 每日签到 */
    @PostMapping("/checkin")
    public R<Void> checkin() {
        pointService.checkin(SecurityUtil.getCurrentUserId());
        return R.ok();
    }
}
