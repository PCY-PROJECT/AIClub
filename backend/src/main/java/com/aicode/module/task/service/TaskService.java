package com.aicode.module.task.service;

import com.aicode.module.task.vo.TaskVO;

import java.util.List;

public interface TaskService {
    /** 获取任务列表，含当前用户完成状态 */
    List<TaskVO> listTasks(Long userId);

    /**
     * 检查任务条件并发放奖励（幂等：已完成则跳过）
     * @param taskType 任务类型标识
     */
    void checkAndReward(Long userId, String taskType);
}
