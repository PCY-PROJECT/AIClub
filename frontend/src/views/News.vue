<template>
  <div class="news-page">
    <!-- Hero -->
    <section class="news-hero">
      <div class="page-container">
        <h1 class="news-hero-title">AI 资讯</h1>
        <p class="news-hero-sub">每日精选 AI 行业动态，保持对 AI 世界的感知</p>
      </div>
    </section>

    <div class="page-container news-body">
      <!-- 分类筛选 -->
      <div class="category-tabs">
        <span
          v-for="tab in categoryTabs"
          :key="tab.value"
          class="category-tab"
          :class="{ active: activeCategory === tab.value }"
          @click="switchCategory(tab.value)"
        >{{ tab.label }}</span>
      </div>

      <!-- 加载骨架 -->
      <div v-if="loading" class="news-list">
        <div v-for="i in 5" :key="i" class="news-card app-card skeleton-card">
          <div class="skeleton" style="width:72px;height:22px;border-radius:6px;margin-bottom:14px"></div>
          <div class="skeleton" style="width:80%;height:20px;margin-bottom:10px"></div>
          <div class="skeleton" style="width:95%;height:14px;margin-bottom:6px"></div>
          <div class="skeleton" style="width:70%;height:14px;margin-bottom:18px"></div>
          <div class="skeleton" style="width:45%;height:12px"></div>
        </div>
      </div>

      <!-- 资讯列表 -->
      <div v-else-if="newsList.length" class="news-list">
        <router-link
          v-for="item in newsList"
          :key="item.id"
          :to="`/news/${item.id}`"
          class="news-card app-card"
          :style="{ '--cat-color': getCategoryColor(item.category) }"
        >
          <!-- 分类标签 + 时间 -->
          <div class="news-card-top">
            <span class="cat-badge" :style="{ background: getCategoryBg(item.category), color: getCategoryColor(item.category) }">
              {{ item.categoryName || '资讯' }}
            </span>
            <span class="news-time">{{ formatRelativeTime(item.publishTime || item.createTime) }}</span>
          </div>

          <!-- 封面 + 内容 -->
          <div class="news-card-body" :class="{ 'has-cover': item.cover }">
            <div class="news-card-text">
              <h3 class="news-title">{{ item.title }}</h3>
              <p v-if="item.summary" class="news-summary">{{ item.summary }}</p>
              <!-- 标签 -->
              <div v-if="item.tags" class="tags-row">
                <span v-for="tag in parseTags(item.tags)" :key="tag" class="tag-chip"># {{ tag }}</span>
              </div>
            </div>
            <img v-if="item.cover" :src="item.cover" class="news-cover-thumb" alt="" />
          </div>

          <!-- 底部元信息 -->
          <div class="news-card-footer">
            <span class="meta-item">
              <el-icon><View /></el-icon>{{ item.viewCount }}
            </span>
            <span class="meta-item">
              <el-icon><StarFilled /></el-icon>{{ item.likeCount }}
            </span>
            <span v-if="item.authorNickname" class="meta-item author">
              <el-icon><User /></el-icon>{{ item.authorNickname }}
            </span>
            <span v-if="item.sourceUrl" class="source-hint">查看原文 →</span>
          </div>
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">暂无相关资讯</div>

      <!-- 加载更多 -->
      <div v-if="!loading && hasMore" class="load-more-wrap">
        <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
      </div>
    </div>

    <!-- 投稿按钮（已登录可见） -->
    <button v-if="authStore.isLoggedIn" class="fab-submit" @click="submitVisible = true" title="投稿资讯">
      <el-icon><Plus /></el-icon>
    </button>

    <!-- 投稿对话框 -->
    <el-dialog v-model="submitVisible" title="投稿资讯" width="520px" :close-on-click-modal="false">
      <div class="submit-form">
        <div class="form-item">
          <label class="form-label">标题 <span class="required">*</span></label>
          <el-input v-model="form.title" placeholder="资讯标题" maxlength="80" show-word-limit />
        </div>
        <div class="form-item">
          <label class="form-label">分类 <span class="required">*</span></label>
          <el-select v-model="form.category" placeholder="选择分类" style="width:100%">
            <el-option v-for="(label, val) in submitCategories" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </div>
        <div class="form-item">
          <label class="form-label">摘要 <span class="required">*</span></label>
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="100字以内的资讯摘要"
            maxlength="100"
            show-word-limit
          />
        </div>
        <div class="form-item">
          <label class="form-label">来源链接 <span class="required">*</span></label>
          <el-input v-model="form.sourceUrl" placeholder="https://..." />
        </div>
        <div class="form-item">
          <label class="form-label">标签 <span class="form-hint">用逗号分隔，如：GPT-4,OpenAI</span></label>
          <el-input v-model="form.tags" placeholder="可选，最多3个标签" />
        </div>
      </div>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交投稿
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View, StarFilled, User, Plus } from '@element-plus/icons-vue'
import { getNewsList, submitNews } from '@/api/news'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/format'
import { NewsCategoryColor } from '@/types'
import type { NewsItem } from '@/types'

const authStore = useAuthStore()

// ─── 列表状态 ─────────────────────────────────────────────
const categoryTabs = [
  { value: 0, label: '全部' },
  { value: 1, label: '产品发布' },
  { value: 2, label: '行业动态' },
  { value: 3, label: '技术突破' },
  { value: 4, label: '使用技巧' },
  { value: 5, label: '政策监管' },
]

const activeCategory = ref(0)
const newsList = ref<NewsItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const hasMore = ref(false)
const PAGE_SIZE = 15

async function loadNews(reset = true) {
  if (reset) {
    loading.value = true
    currentPage.value = 1
    newsList.value = []
  } else {
    loadingMore.value = true
  }
  try {
    const res = await getNewsList({
      category: activeCategory.value || undefined,
      page: currentPage.value,
      size: PAGE_SIZE,
    })
    newsList.value = reset ? res.records : [...newsList.value, ...res.records]
    hasMore.value = newsList.value.length < res.total
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function switchCategory(val: number) {
  activeCategory.value = val
  loadNews(true)
}

async function loadMore() {
  currentPage.value++
  await loadNews(false)
}

// ─── 投稿 ────────────────────────────────────────────────
const submitVisible = ref(false)
const submitting = ref(false)
const submitCategories = { 1: '产品发布', 2: '行业动态', 3: '技术突破', 4: '使用技巧', 5: '政策监管' }
const form = ref({ title: '', category: 1, summary: '', sourceUrl: '', tags: '' })

async function handleSubmit() {
  if (!form.value.title.trim()) { ElMessage.warning('请填写标题'); return }
  if (!form.value.summary.trim()) { ElMessage.warning('请填写摘要'); return }
  if (!form.value.sourceUrl.trim()) { ElMessage.warning('请填写来源链接'); return }
  submitting.value = true
  try {
    await submitNews({
      title: form.value.title.trim(),
      summary: form.value.summary.trim(),
      sourceUrl: form.value.sourceUrl.trim(),
      tags: form.value.tags.trim() || undefined,
      category: form.value.category,
    })
    ElMessage.success('投稿成功！审核通过后将发布并奖励 +20 积分')
    submitVisible.value = false
    form.value = { title: '', category: 1, summary: '', sourceUrl: '', tags: '' }
  } finally {
    submitting.value = false
  }
}

// ─── 工具 ────────────────────────────────────────────────
function getCategoryColor(cat: number) {
  return NewsCategoryColor[cat] ?? '#6a7282'
}
function getCategoryBg(cat: number) {
  const c = getCategoryColor(cat)
  return c + '20'
}
function parseTags(tags: string) {
  return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 3)
}

onMounted(() => loadNews())
</script>

<style scoped>
.news-hero {
  padding: 40px 0 28px;
  text-align: center;
}
.news-hero-title {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #f0f2f5 30%, var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.news-hero-sub {
  font-size: 14px;
  color: var(--color-text-muted);
}
.news-body {
  padding-bottom: 80px;
}
.category-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
.category-tab {
  padding: 5px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  user-select: none;
}
.category-tab:hover, .category-tab.active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
  border-color: var(--color-border-hover);
}
.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 资讯卡片 */
.news-card {
  display: block;
  padding: 20px;
  text-decoration: none;
  cursor: pointer;
  border-left: 3px solid var(--cat-color, var(--color-border));
  transition: border-color 0.2s, box-shadow 0.2s;
}
.news-card:hover {
  border-left-color: var(--cat-color, var(--color-primary));
}
.skeleton-card { cursor: default; border-left-color: var(--color-border) !important; }
.news-card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.cat-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.news-time {
  font-size: 12px;
  color: var(--color-text-disabled);
}
.news-card-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.news-card-text { flex: 1; min-width: 0; }
.news-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news-summary {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tags-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tag-chip {
  font-size: 11px;
  color: var(--color-text-disabled);
  background: var(--color-bg-overlay);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.news-cover-thumb {
  width: 100px;
  height: 68px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.news-card-footer {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-disabled);
}
.meta-item.author { color: var(--color-text-muted); }
.source-hint {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-primary);
  opacity: 0.7;
}
.load-more-wrap {
  text-align: center;
  margin-top: 24px;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

/* 投稿悬浮按钮 */
.fab-submit {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #09090f;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 16px var(--color-primary-glow);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 50;
}
.fab-submit:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px var(--color-primary-glow);
}

/* 投稿表单 */
.submit-form { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label {
  font-size: 13px;
  color: var(--color-text-muted);
  font-weight: 500;
}
.required { color: var(--color-error); }
.form-hint {
  font-size: 11px;
  color: var(--color-text-disabled);
  font-weight: 400;
  margin-left: 4px;
}

@media (max-width: 600px) {
  .fab-submit { bottom: 20px; right: 20px; }
  .news-cover-thumb { width: 72px; height: 50px; }
  .news-title { font-size: 15px; }
}
</style>
