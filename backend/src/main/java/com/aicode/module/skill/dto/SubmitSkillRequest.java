package com.aicode.module.skill.dto;

import lombok.Data;

@Data
public class SubmitSkillRequest {
    private String title;
    /** 提示词正文 */
    private String prompt;
    /** 适用产品，逗号分隔，如 "ChatGPT,Claude" */
    private String applicable;
    /** 分类：写作/编程/设计/营销/分析/翻译/教育/职场/生活 */
    private String category;
    /** 场景标签，逗号分隔 */
    private String tags;
}
