package com.aicode.module.skill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 点赞 / 收藏操作的返回结果 */
@Data
@AllArgsConstructor
public class SkillInteractVO {
    /** 操作后的状态：true=已点赞/已收藏，false=已取消 */
    private boolean active;
    /** 操作后的最新计数 */
    private int count;
}
