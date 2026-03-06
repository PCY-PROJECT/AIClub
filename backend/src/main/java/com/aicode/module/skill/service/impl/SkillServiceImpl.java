package com.aicode.module.skill.service.impl;

import com.aicode.common.exception.BizException;
import com.aicode.module.point.entity.PointRecord;
import com.aicode.module.point.mapper.PointRecordMapper;
import com.aicode.module.point.service.PointService;
import com.aicode.module.skill.dto.SubmitSkillRequest;
import com.aicode.module.skill.entity.Skill;
import com.aicode.module.skill.entity.SkillCollect;
import com.aicode.module.skill.entity.SkillLike;
import com.aicode.module.skill.mapper.SkillCollectMapper;
import com.aicode.module.skill.mapper.SkillLikeMapper;
import com.aicode.module.skill.mapper.SkillMapper;
import com.aicode.module.skill.service.SkillService;
import com.aicode.module.skill.vo.SkillInteractVO;
import com.aicode.module.skill.vo.SkillVO;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillMapper skillMapper;
    private final SkillLikeMapper skillLikeMapper;
    private final SkillCollectMapper skillCollectMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final PointRecordMapper pointRecordMapper;

    // biz_type 常量
    private static final int BIZ_SKILL_SUBMIT   = 9;   // 技能审核通过 +15
    private static final int BIZ_SKILL_COLLECT  = 10;  // 收藏里程碑 +20（每10次）
    private static final int BIZ_SKILL_LIKE     = 11;  // 点赞里程碑 +30（每50次）

    // ─── 公开接口 ──────────────────────────────────────────────────

    @Override
    public Page<SkillVO> list(String category, int pageNum, int pageSize, Long currentUserId) {
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<Skill>()
                .eq(Skill::getStatus, 2)
                .eq(category != null && !category.isBlank(), Skill::getCategory, category)
                .orderByDesc(Skill::getCollectCount)
                .orderByDesc(Skill::getCreateTime);

        Page<Skill> page = skillMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        if (page.getRecords().isEmpty()) return new Page<>(pageNum, pageSize, 0);

        List<Long> skillIds = page.getRecords().stream().map(Skill::getId).collect(Collectors.toList());
        List<Long> authorIds = page.getRecords().stream().map(Skill::getAuthorId).distinct().collect(Collectors.toList());

        Map<Long, String> nicknameMap = userMapper
                .selectList(new LambdaQueryWrapper<User>().in(User::getId, authorIds))
                .stream().collect(Collectors.toMap(User::getId, User::getNickname));

        Set<Long> likedIds    = getLikedSkillIds(skillIds, currentUserId);
        Set<Long> collectedIds = getCollectedSkillIds(skillIds, currentUserId);

        Page<SkillVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(s -> toVO(s, nicknameMap.get(s.getAuthorId()),
                        likedIds.contains(s.getId()),
                        collectedIds.contains(s.getId())))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public SkillVO detail(Long id, Long currentUserId) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null || skill.getStatus() != 2) throw new BizException(404, "技能不存在");
        skillMapper.incrementUseCount(id);
        skill.setUseCount(skill.getUseCount() + 1);

        String nickname = null;
        User author = userMapper.selectById(skill.getAuthorId());
        if (author != null) nickname = author.getNickname();

        boolean liked     = currentUserId != null && isLiked(id, currentUserId);
        boolean collected = currentUserId != null && isCollected(id, currentUserId);
        return toVO(skill, nickname, liked, collected);
    }

    // ─── 提交 / 审核 ───────────────────────────────────────────────

    @Override
    @Transactional
    public void submit(Long userId, SubmitSkillRequest req) {
        validateFields(req.getTitle(), req.getPrompt(), req.getCategory());
        Skill skill = new Skill();
        skill.setTitle(req.getTitle().trim());
        skill.setPrompt(req.getPrompt().trim());
        skill.setApplicable(req.getApplicable());
        skill.setCategory(req.getCategory());
        skill.setTags(req.getTags());
        skill.setAuthorId(userId);
        skill.setStatus(1);  // 待审核
        skill.setLikeCount(0);
        skill.setCollectCount(0);
        skill.setUseCount(0);
        skillMapper.insert(skill);
    }

    @Override
    @Transactional
    public void approve(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) throw new BizException(404, "技能不存在");
        if (skill.getStatus() == 2) throw new BizException(400, "该技能已发布");
        if (skill.getStatus() == 3) throw new BizException(400, "已拒绝的技能无法审核通过");

        skill.setStatus(2);
        skillMapper.updateById(skill);
        pointService.addPoints(skill.getAuthorId(), 15, BIZ_SKILL_SUBMIT,
                String.valueOf(skillId), "提交技能审核通过");
    }

    // ─── 点赞 / 收藏 ───────────────────────────────────────────────

    @Override
    @Transactional
    public SkillInteractVO toggleLike(Long skillId, Long userId) {
        Skill skill = requirePublished(skillId);

        if (isLiked(skillId, userId)) {
            // 取消点赞
            skillLikeMapper.delete(new LambdaQueryWrapper<SkillLike>()
                    .eq(SkillLike::getSkillId, skillId)
                    .eq(SkillLike::getUserId, userId));
            skillMapper.decrementLikeCount(skillId);
            int newCount = Math.max(skill.getLikeCount() - 1, 0);
            return new SkillInteractVO(false, newCount);
        } else {
            // 点赞
            SkillLike like = new SkillLike();
            like.setSkillId(skillId);
            like.setUserId(userId);
            skillLikeMapper.insert(like);
            skillMapper.incrementLikeCount(skillId);
            int newCount = skill.getLikeCount() + 1;
            // 里程碑：每 50 赞奖励作者 +30
            checkAndRewardMilestone(skill.getAuthorId(), newCount, 50,
                    BIZ_SKILL_LIKE, "skill:" + skillId + ":like:" + newCount,
                    30, "技能被点赞 " + newCount + " 次奖励");
            return new SkillInteractVO(true, newCount);
        }
    }

    @Override
    @Transactional
    public SkillInteractVO toggleCollect(Long skillId, Long userId) {
        Skill skill = requirePublished(skillId);

        if (isCollected(skillId, userId)) {
            // 取消收藏
            skillCollectMapper.delete(new LambdaQueryWrapper<SkillCollect>()
                    .eq(SkillCollect::getSkillId, skillId)
                    .eq(SkillCollect::getUserId, userId));
            skillMapper.decrementCollectCount(skillId);
            int newCount = Math.max(skill.getCollectCount() - 1, 0);
            return new SkillInteractVO(false, newCount);
        } else {
            // 收藏
            SkillCollect collect = new SkillCollect();
            collect.setSkillId(skillId);
            collect.setUserId(userId);
            skillCollectMapper.insert(collect);
            skillMapper.incrementCollectCount(skillId);
            int newCount = skill.getCollectCount() + 1;
            // 里程碑：每 10 收藏奖励作者 +20
            checkAndRewardMilestone(skill.getAuthorId(), newCount, 10,
                    BIZ_SKILL_COLLECT, "skill:" + skillId + ":collect:" + newCount,
                    20, "技能被收藏 " + newCount + " 次奖励");
            return new SkillInteractVO(true, newCount);
        }
    }

    @Override
    public Page<SkillVO> mySkills(Long userId, int pageNum, int pageSize) {
        Page<Skill> page = skillMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getAuthorId, userId)
                        .orderByDesc(Skill::getCreateTime));

        Page<SkillVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(s -> toVO(s, null, false, false))
                .collect(Collectors.toList()));
        return voPage;
    }

    // ─── 私有方法 ──────────────────────────────────────────────────

    private Skill requirePublished(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null || skill.getStatus() != 2) throw new BizException(404, "技能不存在");
        return skill;
    }

    private boolean isLiked(Long skillId, Long userId) {
        return skillLikeMapper.selectCount(new LambdaQueryWrapper<SkillLike>()
                .eq(SkillLike::getSkillId, skillId)
                .eq(SkillLike::getUserId, userId)) > 0;
    }

    private boolean isCollected(Long skillId, Long userId) {
        return skillCollectMapper.selectCount(new LambdaQueryWrapper<SkillCollect>()
                .eq(SkillCollect::getSkillId, skillId)
                .eq(SkillCollect::getUserId, userId)) > 0;
    }

    private Set<Long> getLikedSkillIds(List<Long> ids, Long userId) {
        if (userId == null || ids.isEmpty()) return Set.of();
        return skillLikeMapper.selectList(new LambdaQueryWrapper<SkillLike>()
                        .in(SkillLike::getSkillId, ids)
                        .eq(SkillLike::getUserId, userId))
                .stream().map(SkillLike::getSkillId).collect(Collectors.toSet());
    }

    private Set<Long> getCollectedSkillIds(List<Long> ids, Long userId) {
        if (userId == null || ids.isEmpty()) return Set.of();
        return skillCollectMapper.selectList(new LambdaQueryWrapper<SkillCollect>()
                        .in(SkillCollect::getSkillId, ids)
                        .eq(SkillCollect::getUserId, userId))
                .stream().map(SkillCollect::getSkillId).collect(Collectors.toSet());
    }

    /** 里程碑奖励：newCount 为 threshold 的整数倍，且未发放过，则奖励 */
    private void checkAndRewardMilestone(Long authorId, int newCount, int threshold,
                                          int bizType, String bizId, int points, String remark) {
        if (newCount % threshold != 0) return;
        boolean alreadyRewarded = pointRecordMapper.selectCount(
                new LambdaQueryWrapper<PointRecord>()
                        .eq(PointRecord::getBizType, bizType)
                        .eq(PointRecord::getBizId, bizId)
                        .eq(PointRecord::getUserId, authorId)) > 0;
        if (!alreadyRewarded) {
            pointService.addPoints(authorId, points, bizType, bizId, remark);
        }
    }

    private SkillVO toVO(Skill skill, String authorNickname, boolean liked, boolean collected) {
        SkillVO vo = new SkillVO();
        vo.setId(skill.getId());
        vo.setTitle(skill.getTitle());
        vo.setPrompt(skill.getPrompt());
        vo.setApplicable(skill.getApplicable());
        vo.setCategory(skill.getCategory());
        vo.setTags(skill.getTags());
        vo.setAuthorId(skill.getAuthorId());
        vo.setAuthorNickname(authorNickname);
        vo.setLikeCount(skill.getLikeCount());
        vo.setCollectCount(skill.getCollectCount());
        vo.setUseCount(skill.getUseCount());
        vo.setLiked(liked);
        vo.setCollected(collected);
        vo.setCreateTime(skill.getCreateTime());
        vo.setStatus(skill.getStatus());
        return vo;
    }

    private void validateFields(String title, String prompt, String category) {
        if (title == null || title.isBlank()) throw new BizException(400, "标题不能为空");
        if (prompt == null || prompt.isBlank()) throw new BizException(400, "提示词内容不能为空");
        if (category == null || category.isBlank()) throw new BizException(400, "分类不能为空");
    }

    // ─── Admin 方法 ────────────────────────────────────────────

    @Override
    public Page<SkillVO> listAdmin(String category, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<Skill>()
                .eq(category != null && !category.isBlank(), Skill::getCategory, category)
                .eq(status != null, Skill::getStatus, status)
                .orderByDesc(Skill::getCreateTime);
        Page<Skill> page = skillMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<SkillVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(s -> toVO(s, null, false, false)).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void reject(Long skillId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) throw new BizException(404, "技能不存在");
        skill.setStatus(3);
        skillMapper.updateById(skill);
    }
}
