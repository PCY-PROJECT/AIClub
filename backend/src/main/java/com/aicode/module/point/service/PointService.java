package com.aicode.module.point.service;

import com.aicode.module.point.entity.PointRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface PointService {
    /** 增减积分并记录流水（delta 正增负减） */
    void addPoints(Long userId, int delta, int bizType, String bizId, String remark);

    /** 积分流水分页查询 */
    Page<PointRecord> getRecords(Long userId, int pageNum, int pageSize);

    /** 每日签到 */
    void checkin(Long userId);
}
