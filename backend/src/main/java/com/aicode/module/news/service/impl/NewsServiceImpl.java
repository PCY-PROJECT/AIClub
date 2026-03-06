package com.aicode.module.news.service.impl;

import com.aicode.common.exception.BizException;
import com.aicode.module.news.dto.CreateNewsRequest;
import com.aicode.module.news.dto.SubmitNewsRequest;
import com.aicode.module.news.entity.News;
import com.aicode.module.news.entity.NewsLike;
import com.aicode.module.news.mapper.NewsLikeMapper;
import com.aicode.module.news.mapper.NewsMapper;
import com.aicode.module.news.service.NewsService;
import com.aicode.module.news.vo.LikeResultVO;
import com.aicode.module.news.vo.NewsVO;
import com.aicode.module.point.entity.PointRecord;
import com.aicode.module.point.mapper.PointRecordMapper;
import com.aicode.module.point.service.PointService;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;
    private final NewsLikeMapper newsLikeMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final PointRecordMapper pointRecordMapper;

    private static final int BIZ_TYPE_NEWS_SUBMIT  = 7;  // 投稿审核通过 +20
    private static final int BIZ_TYPE_NEWS_LIKE_MILESTONE = 8;  // 获赞里程碑 +30

    private static final String[] CATEGORY_NAMES =
            {"", "产品发布", "行业动态", "技术突破", "使用技巧", "政策监管"};

    // ─── 公开接口 ────────────────────────────────────────────────

    @Override
    public Page<NewsVO> list(Integer category, int pageNum, int pageSize, Long currentUserId) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<News>()
                .eq(News::getStatus, 2)
                .eq(category != null && category != 0, News::getCategory, category)
                .orderByDesc(News::getPublishTime);

        Page<News> page = newsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        if (page.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        // 批量查询 author 昵称
        List<Long> authorIds = page.getRecords().stream()
                .filter(n -> n.getAuthorId() != null)
                .map(News::getAuthorId).distinct().collect(Collectors.toList());
        Map<Long, String> nicknameMap = authorIds.isEmpty() ? new java.util.HashMap<>() :
                userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, authorIds))
                        .stream().collect(Collectors.toMap(User::getId, User::getNickname));

        // 批量查询当前用户的点赞状态
        Set<Long> likedNewsIds = getLikedNewsIds(
                page.getRecords().stream().map(News::getId).collect(Collectors.toList()),
                currentUserId);

        Page<NewsVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(n -> toVO(n, nicknameMap.get(n.getAuthorId()), likedNewsIds.contains(n.getId())))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public NewsVO detail(Long id, Long currentUserId) {
        News news = newsMapper.selectById(id);
        if (news == null || news.getStatus() != 2) {
            throw new BizException(404, "资讯不存在");
        }
        newsMapper.incrementViewCount(id);
        news.setViewCount(news.getViewCount() + 1);

        String authorNickname = null;
        if (news.getAuthorId() != null) {
            User author = userMapper.selectById(news.getAuthorId());
            if (author != null) authorNickname = author.getNickname();
        }

        boolean liked = currentUserId != null && isLiked(id, currentUserId);
        return toVO(news, authorNickname, liked);
    }

    // ─── 创建 / 投稿 ─────────────────────────────────────────────

    @Override
    @Transactional
    public void create(CreateNewsRequest req) {
        validateNewsFields(req.getTitle(), req.getCategory());
        News news = new News();
        news.setTitle(req.getTitle());
        news.setSummary(req.getSummary());
        news.setCover(req.getCover());
        news.setSourceUrl(req.getSourceUrl());
        news.setTags(req.getTags());
        news.setCategory(req.getCategory());
        news.setAuthorId(null);    // 编辑发布
        news.setStatus(2);         // 直接发布
        news.setLikeCount(0);
        news.setViewCount(0);
        news.setPublishTime(LocalDateTime.now());
        newsMapper.insert(news);
    }

    @Override
    @Transactional
    public void submit(Long userId, SubmitNewsRequest req) {
        validateNewsFields(req.getTitle(), req.getCategory());
        News news = new News();
        news.setTitle(req.getTitle());
        news.setSummary(req.getSummary());
        news.setCover(req.getCover());
        news.setSourceUrl(req.getSourceUrl());
        news.setTags(req.getTags());
        news.setCategory(req.getCategory());
        news.setAuthorId(userId);  // 用户投稿
        news.setStatus(1);         // 待审核
        news.setLikeCount(0);
        news.setViewCount(0);
        newsMapper.insert(news);
    }

    @Override
    @Transactional
    public void approve(Long newsId) {
        News news = newsMapper.selectById(newsId);
        if (news == null) throw new BizException(404, "资讯不存在");
        if (news.getStatus() == 2) throw new BizException(400, "该资讯已发布");
        if (news.getStatus() == 3) throw new BizException(400, "该资讯已被拒绝，无法审核通过");

        news.setStatus(2);
        news.setPublishTime(LocalDateTime.now());
        newsMapper.updateById(news);

        // 奖励投稿用户积分
        if (news.getAuthorId() != null) {
            pointService.addPoints(news.getAuthorId(), 20, BIZ_TYPE_NEWS_SUBMIT,
                    String.valueOf(newsId), "投稿资讯审核通过");
        }
    }

    // ─── 点赞 ───────────────────────────────────────────────────

    @Override
    @Transactional
    public LikeResultVO toggleLike(Long newsId, Long userId) {
        News news = newsMapper.selectById(newsId);
        if (news == null || news.getStatus() != 2) {
            throw new BizException(404, "资讯不存在");
        }

        boolean alreadyLiked = isLiked(newsId, userId);
        if (alreadyLiked) {
            // 取消点赞
            newsLikeMapper.delete(new LambdaQueryWrapper<NewsLike>()
                    .eq(NewsLike::getNewsId, newsId)
                    .eq(NewsLike::getUserId, userId));
            newsMapper.decrementLikeCount(newsId);
            int newCount = Math.max(news.getLikeCount() - 1, 0);
            return new LikeResultVO(false, newCount);
        } else {
            // 点赞
            NewsLike like = new NewsLike();
            like.setNewsId(newsId);
            like.setUserId(userId);
            newsLikeMapper.insert(like);
            newsMapper.incrementLikeCount(newsId);
            int newCount = news.getLikeCount() + 1;

            // 检查里程碑奖励（每10个赞奖励一次）
            if (newCount % 10 == 0 && news.getAuthorId() != null) {
                String milestoneKey = "news:" + newsId + ":like:" + newCount;
                boolean alreadyRewarded = pointRecordMapper.selectCount(
                        new LambdaQueryWrapper<PointRecord>()
                                .eq(PointRecord::getBizType, BIZ_TYPE_NEWS_LIKE_MILESTONE)
                                .eq(PointRecord::getBizId, milestoneKey)
                                .eq(PointRecord::getUserId, news.getAuthorId())
                ) > 0;
                if (!alreadyRewarded) {
                    pointService.addPoints(news.getAuthorId(), 30, BIZ_TYPE_NEWS_LIKE_MILESTONE,
                            milestoneKey, "投稿资讯获赞 " + newCount + " 次奖励");
                }
            }
            return new LikeResultVO(true, newCount);
        }
    }

    // ─── 私有方法 ────────────────────────────────────────────────

    private boolean isLiked(Long newsId, Long userId) {
        return newsLikeMapper.selectCount(new LambdaQueryWrapper<NewsLike>()
                .eq(NewsLike::getNewsId, newsId)
                .eq(NewsLike::getUserId, userId)) > 0;
    }

    private Set<Long> getLikedNewsIds(List<Long> newsIds, Long userId) {
        if (userId == null || newsIds.isEmpty()) return Set.of();
        return newsLikeMapper.selectList(new LambdaQueryWrapper<NewsLike>()
                        .in(NewsLike::getNewsId, newsIds)
                        .eq(NewsLike::getUserId, userId))
                .stream().map(NewsLike::getNewsId).collect(Collectors.toSet());
    }

    private NewsVO toVO(News news, String authorNickname, boolean liked) {
        NewsVO vo = new NewsVO();
        vo.setId(news.getId());
        vo.setTitle(news.getTitle());
        vo.setSummary(news.getSummary());
        vo.setCover(news.getCover());
        vo.setSourceUrl(news.getSourceUrl());
        vo.setTags(news.getTags());
        vo.setCategory(news.getCategory());
        if (news.getCategory() != null
                && news.getCategory() >= 1
                && news.getCategory() <= CATEGORY_NAMES.length - 1) {
            vo.setCategoryName(CATEGORY_NAMES[news.getCategory()]);
        }
        vo.setAuthorId(news.getAuthorId());
        vo.setAuthorNickname(authorNickname);
        vo.setLikeCount(news.getLikeCount());
        vo.setViewCount(news.getViewCount());
        vo.setLiked(liked);
        vo.setPublishTime(news.getPublishTime());
        vo.setCreateTime(news.getCreateTime());
        vo.setStatus(news.getStatus());
        return vo;
    }

    private void validateNewsFields(String title, Integer category) {
        if (title == null || title.isBlank()) {
            throw new BizException(400, "标题不能为空");
        }
        if (category == null || category < 1 || category > 5) {
            throw new BizException(400, "分类参数有误，请选择 1-5");
        }
    }

    // ─── Admin 方法 ────────────────────────────────────────────

    @Override
    public Page<NewsVO> listAdmin(Integer category, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<News>()
                .eq(category != null && category != 0, News::getCategory, category)
                .eq(status != null, News::getStatus, status)
                .orderByDesc(News::getCreateTime);
        Page<News> page = newsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<NewsVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(n -> toVO(n, null, false)).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void reject(Long newsId) {
        News news = newsMapper.selectById(newsId);
        if (news == null) throw new BizException(404, "资讯不存在");
        news.setStatus(3);
        newsMapper.updateById(news);
    }

    @Override
    @Transactional
    public void offline(Long newsId) {
        News news = newsMapper.selectById(newsId);
        if (news == null) throw new BizException(404, "资讯不存在");
        if (news.getStatus() != 2) throw new BizException(400, "只有已发布的资讯才能下线");
        news.setStatus(4);
        newsMapper.updateById(news);
    }
}
