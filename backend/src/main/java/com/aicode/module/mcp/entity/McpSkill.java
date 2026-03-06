package com.aicode.module.mcp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_mcp_skill")
public class McpSkill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;     // 功能描述（面向普通用户）
    /** 类型：1 MCP Server  2 Agent Skill */
    private Integer type;
    private String category;        // 分类（文件管理/浏览器/代码开发/通讯协作...）
    private String sourceUrl;       // GitHub/官网链接
    private String installGuide;    // 安装/使用指南（Markdown）
    private String vendor;          // 开发者/组织
    private String tags;
    /** 状态：1待审核 2已上架 3已下架 */
    private Integer status;
    private Integer collectCount;
    private Integer useCount;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
