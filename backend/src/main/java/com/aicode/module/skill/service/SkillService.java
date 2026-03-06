package com.aicode.module.skill.service;

import com.aicode.module.skill.dto.SubmitSkillRequest;
import com.aicode.module.skill.vo.SkillInteractVO;
import com.aicode.module.skill.vo.SkillVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SkillService {

    /** 已发布技能列表（公开），按 category 筛选 */
    Page<SkillVO> list(String category, int pageNum, int pageSize, Long currentUserId);

    /** 技能详情，use_count+1（公开） */
    SkillVO detail(Long id, Long currentUserId);

    /** 用户提交技能（status=1 待审核） */
    void submit(Long userId, SubmitSkillRequest req);

    /** 审核通过，发放 +15 积分 */
    void approve(Long skillId);

    /** 点赞 / 取消点赞，每50赞触发 +30 里程碑 */
    SkillInteractVO toggleLike(Long skillId, Long userId);

    /** 收藏 / 取消收藏，每10收藏触发 +20 里程碑 */
    SkillInteractVO toggleCollect(Long skillId, Long userId);

    /** 我提交的技能（含待审核），分页 */
    Page<SkillVO> mySkills(Long userId, int pageNum, int pageSize);

    /** 管理员列表（全状态可筛选） */
    Page<SkillVO> listAdmin(String category, Integer status, int pageNum, int pageSize);

    /** 审核拒绝 */
    void reject(Long skillId);
}
