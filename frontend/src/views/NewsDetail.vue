<template>
  <div class="news-detail-page page-container">
    <!-- 返回 -->
    <router-link to="/news" class="back-link">
      <el-icon><ArrowLeft /></el-icon>
      返回资讯列表
    </router-link>

    <!-- 骨架屏 -->
    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton" style="width:80px;height:24px;border-radius:6px;margin-bottom:16px"></div>
      <div class="skeleton" style="width:90%;height:32px;margin-bottom:10px"></div>
      <div class="skeleton" style="width:65%;height:32px;margin-bottom:20px"></div>
      <div class="skeleton" style="width:50%;height:14px;margin-bottom:24px"></div>
      <div class="skeleton" style="width:100%;height:100px;border-radius:10px;margin-bottom:20px"></div>
    </div>

    <!-- 正文 -->
    <template v-else-if="news">
      <div class="detail-header">
        <!-- 分类 + 时间 -->
        <div class="detail-meta-top">
          <span
            class="cat-badge"
            :style="{ background: getCategoryBg(news.category), color: getCategoryColor(news.category) }"
          >{{ news.categoryName || '资讯' }}</span>
          <span class="meta-time">{{ formatRelativeTime(news.publishTime || news.createTime) }}</span>
          <span class="meta-views"><el-icon><View /></el-icon>{{ news.viewCount }} 次浏览</span>
        </div>

        <!-- 标题 -->
        <h1 class="detail-title">{{ news.title }}</h1>

        <!-- 作者 -->
        <div v-if="news.authorNickname" class="detail-author">
          <el-icon><User /></el-icon>
          投稿者：{{ news.authorNickname }}
        </div>
      </div>

      <!-- 封面图 -->
      <img v-if="news.cover" :src="news.cover" class="detail-cover" alt="" />

      <!-- 摘要 -->
      <div class="detail-summary">
        <p>{{ news.summary }}</p>
      </div>

      <!-- 标签 -->
      <div v-if="news.tags" class="detail-tags">
        <span v-for="tag in parseTags(news.tags)" :key="tag" class="tag-chip"># {{ tag }}</span>
      </div>

      <!-- 原文链接 -->
      <a
        v-if="news.sourceUrl"
        :href="news.sourceUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="source-btn"
        @click.stop
      >
        阅读原文
        <el-icon><TopRight /></el-icon>
      </a>

      <!-- 点赞区域 -->
      <div class="like-section">
        <button class="like-btn" :class="{ liked: news.liked }" @click="handleLike">
          <el-icon>
            <StarFilled v-if="news.liked" />
            <Star v-else />
          </el-icon>
          <span class="like-count">{{ news.likeCount }}</span>
          <span class="like-label">{{ news.liked ? '已点赞' : '点赞' }}</span>
        </button>
      </div>

      <!-- 相关资讯 -->
      <div v-if="relatedNews.length" class="related-section">
        <h2 class="related-title">相关资讯</h2>
        <div class="related-grid">
          <router-link
            v-for="item in relatedNews"
            :key="item.id"
            :to="`/news/${item.id}`"
            class="related-card app-card"
          >
            <span
              class="cat-badge-sm"
              :style="{ background: getCategoryBg(item.category), color: getCategoryColor(item.category) }"
            >{{ item.categoryName }}</span>
            <p class="related-card-title">{{ item.title }}</p>
            <span class="related-card-time">{{ formatRelativeTime(item.publishTime || item.createTime) }}</span>
          </router-link>
        </div>
      </div>
    </template>

    <!-- 404 -->
    <div v-else class="not-found">资讯不存在或已下线</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, User, StarFilled, Star, TopRight } from '@element-plus/icons-vue'
import { getNewsDetail, getNewsList, likeNews } from '@/api/news'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/format'
import { NewsCategoryColor } from '@/types'
import type { NewsItem } from '@/types'

const route = useRoute()
const authStore = useAuthStore()

const news = ref<NewsItem | null>(null)
const relatedNews = ref<NewsItem[]>([])
const loading = ref(false)

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  news.value = null
  relatedNews.value = []
  try {
    news.value = await getNewsDetail(id)
    // 加载相关资讯（同分类，排除自身）
    const res = await getNewsList({ category: news.value.category, size: 4 })
    relatedNews.value = res.records.filter(n => n.id !== id).slice(0, 3)
  } catch {
    // 404 由请求拦截器提示
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  if (!news.value) return
  try {
    const res = await likeNews(news.value.id)
    news.value.liked = res.liked
    news.value.likeCount = res.likeCount
    ElMessage.success(res.liked ? '点赞成功' : '已取消点赞')
  } catch {
    // 错误由拦截器统一提示
  }
}

function getCategoryColor(cat: number) {
  return NewsCategoryColor[cat] ?? '#6a7282'
}
function getCategoryBg(cat: number) {
  return getCategoryColor(cat) + '20'
}
function parseTags(tags: string) {
  return tags.split(',').map(t => t.trim()).filter(Boolean)
}

onMounted(loadDetail)
// 从相关资讯跳转到新的详情页时重新加载
watch(() => route.params.id, loadDetail)
</script>

<style scoped>
.news-detail-page {
  padding-top: 28px;
  padding-bottom: 80px;
  max-width: 780px;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
  text-decoration: none;
  margin-bottom: 28px;
  transition: color 0.2s;
}
.back-link:hover { color: var(--color-primary); }

/* 骨架 */
.detail-skeleton { display: flex; flex-direction: column; }

/* 头部 */
.detail-header { margin-bottom: 20px; }
.detail-meta-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.cat-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
}
.meta-time, .meta-views {
  font-size: 13px;
  color: var(--color-text-disabled);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.detail-title {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.4;
  margin: 0 0 12px;
}
.detail-author {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--color-text-muted);
}

/* 封面 */
.detail-cover {
  width: 100%;
  max-height: 320px;
  object-fit: cover;
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
}

/* 摘要 */
.detail-summary {
  background: var(--color-bg-overlay);
  border-left: 3px solid var(--color-primary);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.detail-summary p {
  font-size: 15px;
  color: var(--color-text-muted);
  line-height: 1.75;
  margin: 0;
}

/* 标签 */
.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.tag-chip {
  font-size: 12px;
  color: var(--color-text-disabled);
  background: var(--color-bg-overlay);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
}

/* 原文按钮 */
.source-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: var(--color-primary-dim);
  color: var(--color-primary);
  border: 1px solid var(--color-border-hover);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
  margin-bottom: 32px;
}
.source-btn:hover {
  background: rgba(0, 210, 239, 0.25);
  box-shadow: var(--shadow-glow);
}

/* 点赞区域 */
.like-section {
  display: flex;
  justify-content: center;
  padding: 24px 0 32px;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 40px;
}
.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 28px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-muted);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}
.like-btn:hover {
  border-color: var(--color-border-hover);
  color: var(--color-primary);
}
.like-btn.liked {
  background: var(--color-primary-dim);
  border-color: var(--color-border-hover);
  color: var(--color-primary);
}
.like-count {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 16px;
}
.like-label { font-size: 13px; }

/* 相关资讯 */
.related-section { margin-top: 8px; }
.related-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 16px;
}
.related-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.related-card {
  padding: 14px;
  text-decoration: none;
  display: block;
  cursor: pointer;
}
.cat-badge-sm {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  display: inline-block;
  margin-bottom: 8px;
}
.related-card-title {
  font-size: 13px;
  color: var(--color-text-primary);
  font-weight: 500;
  line-height: 1.45;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.related-card-time {
  font-size: 11px;
  color: var(--color-text-disabled);
}
.not-found {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-muted);
}

@media (max-width: 600px) {
  .detail-title { font-size: 20px; }
  .related-grid { grid-template-columns: 1fr; }
}
</style>
