package com.aicode.module.mcp.dto;

import lombok.Data;

@Data
public class CreateMcpSkillRequest {
    private String name;
    private String description;
    private Integer type;           // 1 MCP Server  2 Agent Skill
    private String category;
    private String sourceUrl;
    private String installGuide;
    private String vendor;
    private String tags;
    private Integer sort;
}
