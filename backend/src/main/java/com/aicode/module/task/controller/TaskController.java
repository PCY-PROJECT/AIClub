package com.aicode.module.task.controller;

import com.aicode.common.result.R;
import com.aicode.common.security.SecurityUtil;
import com.aicode.module.task.service.TaskService;
import com.aicode.module.task.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** 任务列表（含当前用户完成状态） */
    @GetMapping
    public R<List<TaskVO>> tasks() {
        return R.ok(taskService.listTasks(SecurityUtil.getCurrentUserId()));
    }
}
