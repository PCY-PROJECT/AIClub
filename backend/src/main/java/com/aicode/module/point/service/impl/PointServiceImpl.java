package com.aicode.module.point.service.impl;

import com.aicode.common.cache.InMemoryCacheService;
import com.aicode.common.exception.BizException;
import com.aicode.module.point.entity.PointRecord;
import com.aicode.module.point.mapper.PointRecordMapper;
import com.aicode.module.point.service.PointService;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserMapper userMapper;
    private final PointRecordMapper pointRecordMapper;
    private final InMemoryCacheService cache;

    @Override
    @Transactional
    public void addPoints(Long userId, int delta, int bizType, String bizId, String remark) {
        int affected = userMapper.addPoints(userId, delta);
        if (affected == 0) {
            throw new BizException(400, "积分不足");
        }
        User user = userMapper.selectById(userId);
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setDelta(delta);
        record.setBalance(user.getPoints());
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setRemark(remark);
        pointRecordMapper.insert(record);
    }

    @Override
    public Page<PointRecord> getRecords(Long userId, int pageNum, int pageSize) {
        return pointRecordMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<PointRecord>()
                        .eq(PointRecord::getUserId, userId)
                        .orderByDesc(PointRecord::getCreateTime)
        );
    }

    @Override
    @Transactional
    public void checkin(Long userId) {
        String key = "checkin:" + userId + ":" + LocalDate.now();
        // setIfAbsent 是原子操作：key 不存在时写入并返回 true，已存在返回 false
        boolean isNew = cache.setIfAbsent(key, "1", Duration.ofDays(2));
        if (!isNew) {
            throw new BizException(400, "今日已签到，明天再来");
        }
        addPoints(userId, 10, 2, null, "每日签到");
    }
}
