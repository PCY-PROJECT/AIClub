package com.aicode.module.mcp.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class McpSkillVO {
    private Long id;
    private String name;
    private String description;
    private Integer type;
    private String typeName;        // MCP Server / Agent Skill
    private String category;
    private String sourceUrl;
    private String installGuide;
    private String vendor;
    private String tags;
    private Integer status;
    private Integer collectCount;
    private Integer useCount;
    private Integer sort;
    private LocalDateTime createTime;
}
