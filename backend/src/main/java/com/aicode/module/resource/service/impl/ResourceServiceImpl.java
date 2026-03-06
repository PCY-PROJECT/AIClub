package com.aicode.module.resource.service.impl;

import com.aicode.common.exception.BizException;
import com.aicode.module.resource.dto.CreateResourceRequest;
import com.aicode.module.resource.dto.SubmitResourceRequest;
import com.aicode.module.resource.entity.Resource;
import com.aicode.module.resource.entity.ResourceCollect;
import com.aicode.module.resource.mapper.ResourceCollectMapper;
import com.aicode.module.resource.mapper.ResourceMapper;
import com.aicode.module.resource.service.ResourceService;
import com.aicode.module.resource.vo.CollectResultVO;
import com.aicode.module.resource.vo.ResourceVO;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final ResourceCollectMapper resourceCollectMapper;
    private final UserMapper userMapper;

    private static final String[] CATEGORY_NAMES =
            {"", "大模型", "Agent框架", "MCP", "教程", "工具评测", "行业洞察"};
    private static final String[] DIFFICULTY_NAMES =
            {"", "入门", "进阶", "专业"};

    @Override
    public Page<ResourceVO> list(Integer category, Integer difficulty,
                                  int pageNum, int pageSize, Long currentUserId) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 2)
                .eq(category != null && category != 0, Resource::getCategory, category)
                .eq(difficulty != null && difficulty != 0, Resource::getDifficulty, difficulty)
                .orderByDesc(Resource::getPublishTime);

        Page<Resource> page = resourceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        if (page.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        // 批量查询投稿用户昵称
        List<Long> userIds = page.getRecords().stream()
                .filter(r -> r.getSubmitUserId() != null)
                .map(Resource::getSubmitUserId).distinct().collect(Collectors.toList());
        Map<Long, String> nicknameMap = userIds.isEmpty() ? new HashMap<>() :
                userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds))
                        .stream().collect(Collectors.toMap(User::getId, User::getNickname));

        // 批量查询收藏状态
        Set<Long> collectedIds = getCollectedIds(
                page.getRecords().stream().map(Resource::getId).collect(Collectors.toList()),
                currentUserId);

        Page<ResourceVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(r -> toVO(r, nicknameMap.get(r.getSubmitUserId()), collectedIds.contains(r.getId())))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public ResourceVO detail(Long id, Long currentUserId) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || resource.getStatus() != 2) {
            throw new BizException(404, "内容不存在");
        }
        resourceMapper.incrementViewCount(id);
        resource.setViewCount(resource.getViewCount() + 1);

        String nickname = null;
        if (resource.getSubmitUserId() != null) {
            User user = userMapper.selectById(resource.getSubmitUserId());
            if (user != null) nickname = user.getNickname();
        }

        boolean collected = currentUserId != null && isCollected(id, currentUserId);
        return toVO(resource, nickname, collected);
    }

    @Override
    @Transactional
    public void create(CreateResourceRequest req) {
        validateFields(req.getTitle(), req.getSourceUrl(), req.getCategory());
        Resource r = buildResource(req.getTitle(), req.getSummary(), req.getSourceUrl(),
                req.getCover(), req.getAuthor(), req.getCategory(), req.getDifficulty(), req.getTags());
        r.setSubmitUserId(null);
        r.setStatus(2);
        r.setPublishTime(LocalDateTime.now());
        resourceMapper.insert(r);
    }

    @Override
    @Transactional
    public void submit(Long userId, SubmitResourceRequest req) {
        validateFields(req.getTitle(), req.getSourceUrl(), req.getCategory());
        Resource r = buildResource(req.getTitle(), req.getSummary(), req.getSourceUrl(),
                req.getCover(), req.getAuthor(), req.getCategory(), req.getDifficulty(), req.getTags());
        r.setSubmitUserId(userId);
        r.setStatus(1);  // 待审核
        resourceMapper.insert(r);
    }

    @Override
    @Transactional
    public void approve(Long resourceId) {
        Resource r = resourceMapper.selectById(resourceId);
        if (r == null) throw new BizException(404, "内容不存在");
        if (r.getStatus() == 2) throw new BizException(400, "已发布");
        r.setStatus(2);
        r.setPublishTime(LocalDateTime.now());
        resourceMapper.updateById(r);
    }

    @Override
    @Transactional
    public void reject(Long resourceId) {
        Resource r = resourceMapper.selectById(resourceId);
        if (r == null) throw new BizException(404, "内容不存在");
        r.setStatus(3);
        resourceMapper.updateById(r);
    }

    @Override
    @Transactional
    public CollectResultVO toggleCollect(Long resourceId, Long userId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null || resource.getStatus() != 2) {
            throw new BizException(404, "内容不存在");
        }

        boolean alreadyCollected = isCollected(resourceId, userId);
        if (alreadyCollected) {
            resourceCollectMapper.delete(new LambdaQueryWrapper<ResourceCollect>()
                    .eq(ResourceCollect::getResourceId, resourceId)
                    .eq(ResourceCollect::getUserId, userId));
            resourceMapper.decrementCollectCount(resourceId);
            return new CollectResultVO(false, Math.max(resource.getCollectCount() - 1, 0));
        } else {
            ResourceCollect collect = new ResourceCollect();
            collect.setResourceId(resourceId);
            collect.setUserId(userId);
            resourceCollectMapper.insert(collect);
            resourceMapper.incrementCollectCount(resourceId);
            return new CollectResultVO(true, resource.getCollectCount() + 1);
        }
    }

    // ─── 私有方法 ────────────────────────────────────────────────

    private boolean isCollected(Long resourceId, Long userId) {
        return resourceCollectMapper.selectCount(new LambdaQueryWrapper<ResourceCollect>()
                .eq(ResourceCollect::getResourceId, resourceId)
                .eq(ResourceCollect::getUserId, userId)) > 0;
    }

    private Set<Long> getCollectedIds(List<Long> ids, Long userId) {
        if (userId == null || ids.isEmpty()) return Set.of();
        return resourceCollectMapper.selectList(new LambdaQueryWrapper<ResourceCollect>()
                        .in(ResourceCollect::getResourceId, ids)
                        .eq(ResourceCollect::getUserId, userId))
                .stream().map(ResourceCollect::getResourceId).collect(Collectors.toSet());
    }

    private Resource buildResource(String title, String summary, String sourceUrl,
                                    String cover, String author,
                                    Integer category, Integer difficulty, String tags) {
        Resource r = new Resource();
        r.setTitle(title);
        r.setSummary(summary);
        r.setSourceUrl(sourceUrl);
        r.setCover(cover);
        r.setAuthor(author);
        r.setCategory(category != null ? category : 1);
        r.setDifficulty(difficulty != null ? difficulty : 1);
        r.setTags(tags);
        r.setViewCount(0);
        r.setCollectCount(0);
        r.setIsAutoFetched(0);
        return r;
    }

    private ResourceVO toVO(Resource r, String nickname, boolean collected) {
        ResourceVO vo = new ResourceVO();
        vo.setId(r.getId());
        vo.setTitle(r.getTitle());
        vo.setSummary(r.getSummary());
        vo.setSourceUrl(r.getSourceUrl());
        vo.setCover(r.getCover());
        vo.setAuthor(r.getAuthor());
        vo.setCategory(r.getCategory());
        if (r.getCategory() != null && r.getCategory() >= 1 && r.getCategory() <= 6) {
            vo.setCategoryName(CATEGORY_NAMES[r.getCategory()]);
        }
        vo.setDifficulty(r.getDifficulty());
        if (r.getDifficulty() != null && r.getDifficulty() >= 1 && r.getDifficulty() <= 3) {
            vo.setDifficultyName(DIFFICULTY_NAMES[r.getDifficulty()]);
        }
        vo.setTags(r.getTags());
        vo.setStatus(r.getStatus());
        vo.setViewCount(r.getViewCount());
        vo.setCollectCount(r.getCollectCount());
        vo.setSubmitUserId(r.getSubmitUserId());
        vo.setSubmitUserNickname(nickname);
        vo.setCollected(collected);
        vo.setPublishTime(r.getPublishTime());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }

    private void validateFields(String title, String sourceUrl, Integer category) {
        if (title == null || title.isBlank()) throw new BizException(400, "标题不能为空");
        if (sourceUrl == null || sourceUrl.isBlank()) throw new BizException(400, "来源链接不能为空");
        if (category == null || category < 1 || category > 6) {
            throw new BizException(400, "分类参数有误，请选择 1-6");
        }
    }

    // ─── Admin 方法 ────────────────────────────────────────────

    @Override
    public Page<ResourceVO> listAdmin(Integer category, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<Resource>()
                .eq(category != null && category != 0, Resource::getCategory, category)
                .eq(status != null, Resource::getStatus, status)
                .orderByDesc(Resource::getCreateTime);
        Page<Resource> page = resourceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<ResourceVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(r -> toVO(r, null, false)).collect(Collectors.toList()));
        return voPage;
    }
}
