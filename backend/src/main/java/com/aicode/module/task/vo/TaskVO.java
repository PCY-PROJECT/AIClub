package com.aicode.module.task.vo;

import lombok.Data;

@Data
public class TaskVO {
    private Long id;
    private String name;
    private String taskType;
    /** 1每日 2每周 3一次性 */
    private Integer frequency;
    private Integer pointsReward;
    /** 当前用户是否已完成（每日任务：今天；一次性：是否完成过） */
    private boolean completed;
}
