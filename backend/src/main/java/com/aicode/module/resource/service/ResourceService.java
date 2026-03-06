package com.aicode.module.resource.service;

import com.aicode.module.resource.dto.CreateResourceRequest;
import com.aicode.module.resource.dto.SubmitResourceRequest;
import com.aicode.module.resource.vo.CollectResultVO;
import com.aicode.module.resource.vo.ResourceVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ResourceService {

    Page<ResourceVO> list(Integer category, Integer difficulty, int pageNum, int pageSize, Long currentUserId);

    ResourceVO detail(Long id, Long currentUserId);

    void create(CreateResourceRequest req);

    void submit(Long userId, SubmitResourceRequest req);

    void approve(Long resourceId);

    void reject(Long resourceId);

    CollectResultVO toggleCollect(Long resourceId, Long userId);

    /** 管理员列表（全状态可筛选） */
    Page<ResourceVO> listAdmin(Integer category, Integer status, int pageNum, int pageSize);
}
