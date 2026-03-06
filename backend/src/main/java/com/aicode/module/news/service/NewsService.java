package com.aicode.module.news.service;

import com.aicode.module.news.dto.CreateNewsRequest;
import com.aicode.module.news.dto.SubmitNewsRequest;
import com.aicode.module.news.vo.LikeResultVO;
import com.aicode.module.news.vo.NewsVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface NewsService {

    /** 已发布资讯列表（公开） */
    Page<NewsVO> list(Integer category, int pageNum, int pageSize, Long currentUserId);

    /** 资讯详情，浏览数+1（公开） */
    NewsVO detail(Long id, Long currentUserId);

    /** 编辑直接创建并发布资讯 */
    void create(CreateNewsRequest req);

    /** 用户投稿资讯（status=1 待审核） */
    void submit(Long userId, SubmitNewsRequest req);

    /** 审核通过并发放积分 */
    void approve(Long newsId);

    /** 点赞/取消点赞，返回最新状态 */
    LikeResultVO toggleLike(Long newsId, Long userId);

    /** 管理员列表（全状态可筛选） */
    Page<NewsVO> listAdmin(Integer category, Integer status, int pageNum, int pageSize);

    /** 审核拒绝 */
    void reject(Long newsId);

    /** 下线（status 2→4） */
    void offline(Long newsId);
}
