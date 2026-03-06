package com.aicode.module.task.service.impl;

import com.aicode.module.code.entity.InviteCode;
import com.aicode.module.code.mapper.InviteCodeMapper;
import com.aicode.module.point.entity.PointRecord;
import com.aicode.module.point.mapper.PointRecordMapper;
import com.aicode.module.task.entity.Task;
import com.aicode.module.task.entity.TaskRecord;
import com.aicode.module.task.mapper.TaskMapper;
import com.aicode.module.task.mapper.TaskRecordMapper;
import com.aicode.module.task.service.TaskService;
import com.aicode.module.task.vo.TaskVO;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskRecordMapper taskRecordMapper;
    private final UserMapper userMapper;
    private final PointRecordMapper pointRecordMapper;
    private final InviteCodeMapper inviteCodeMapper;

    @Override
    public List<TaskVO> listTasks(Long userId) {
        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getStatus, 1)
                        .orderByAsc(Task::getSort));

        return tasks.stream().map(task -> {
            TaskVO vo = new TaskVO();
            vo.setId(task.getId());
            vo.setName(task.getName());
            vo.setTaskType(task.getTaskType());
            vo.setFrequency(task.getFrequency());
            vo.setPointsReward(task.getPointsReward());
            vo.setCompleted(isCompleted(userId, task));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void checkAndReward(Long userId, String taskType) {
        Task task = taskMapper.selectOne(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getTaskType, taskType)
                        .eq(Task::getStatus, 1));
        if (task == null) return;
        if (isCompleted(userId, task)) return;

        // SUBMIT_CODE_5_TIMES 需额外检查提交数量
        if ("SUBMIT_CODE_5_TIMES".equals(taskType)) {
            long submitCount = inviteCodeMapper.selectCount(
                    new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getProviderId, userId));
            if (submitCount < 5) return;
        }

        // 记录任务完成
        TaskRecord record = new TaskRecord();
        record.setUserId(userId);
        record.setTaskId(task.getId());
        record.setTaskType(taskType);
        taskRecordMapper.insert(record);

        // 发放积分奖励
        userMapper.addPoints(userId, task.getPointsReward());
        User user = userMapper.selectById(userId);

        PointRecord pr = new PointRecord();
        pr.setUserId(userId);
        pr.setDelta(task.getPointsReward());
        pr.setBalance(user.getPoints());
        pr.setBizType(6);
        pr.setBizId(String.valueOf(task.getId()));
        pr.setRemark("完成任务：" + task.getName());
        pointRecordMapper.insert(pr);

        log.info("用户 {} 完成任务 {}，获得 {} 积分", userId, taskType, task.getPointsReward());
    }

    // ─── 私有方法 ─────────────────────────────────────────────

    private boolean isCompleted(Long userId, Task task) {
        LambdaQueryWrapper<TaskRecord> wrapper = new LambdaQueryWrapper<TaskRecord>()
                .eq(TaskRecord::getUserId, userId)
                .eq(TaskRecord::getTaskType, task.getTaskType());

        if (task.getFrequency() == 1) {
            // 每日任务：检查今天
            wrapper.ge(TaskRecord::getCreateTime, LocalDate.now().atStartOfDay());
        } else if (task.getFrequency() == 2) {
            // 每周任务：检查本周一以来
            LocalDateTime weekStart = LocalDate.now()
                    .with(DayOfWeek.MONDAY).atStartOfDay();
            wrapper.ge(TaskRecord::getCreateTime, weekStart);
        }
        // frequency=3（一次性）：只要记录存在即视为完成

        return taskRecordMapper.exists(wrapper);
    }
}
