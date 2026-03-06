package com.aicode.module.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;         // 摘要（150字内）
    private String sourceUrl;       // 来源原文链接（核心字段）
    private String cover;           // 封面图
    private String author;          // 原文作者/来源媒体
    /** 分类：1大模型 2Agent框架 3MCP 4教程 5工具评测 6行业洞察 */
    private Integer category;
    /** 难度：1入门 2进阶 3专业 */
    private Integer difficulty;
    private String tags;
    /** 状态：1待审核 2已发布 3已拒绝 */
    private Integer status;
    private Integer viewCount;
    private Integer collectCount;
    private Integer isAutoFetched;  // 0手动录入 1自动抓取
    private Long submitUserId;      // NULL=编辑录入
    private LocalDateTime publishTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
