<template>
  <div class="skill-detail-page page-container">
    <!-- 返回 -->
    <router-link to="/skills" class="back-link">
      <el-icon><ArrowLeft /></el-icon>
      返回技能广场
    </router-link>

    <!-- 骨架屏 -->
    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton" style="width:72px;height:24px;border-radius:6px;margin-bottom:16px"></div>
      <div class="skeleton" style="width:85%;height:30px;margin-bottom:10px"></div>
      <div class="skeleton" style="width:50%;height:14px;margin-bottom:24px"></div>
      <div class="skeleton" style="width:100%;height:140px;border-radius:10px;margin-bottom:20px"></div>
    </div>

    <!-- 正文 -->
    <template v-else-if="skill">
      <div class="detail-header">
        <!-- 分类 + 时间 -->
        <div class="detail-meta-top">
          <span
            class="cat-badge"
            :style="{ background: getCategoryBg(skill.category), color: getCategoryColor(skill.category) }"
          >{{ skill.category }}</span>
          <span class="meta-time">{{ formatRelativeTime(skill.createTime) }}</span>
          <span class="meta-stat"><el-icon><View /></el-icon>{{ skill.useCount }} 次使用</span>
        </div>

        <!-- 标题 -->
        <h1 class="detail-title">{{ skill.title }}</h1>

        <!-- 作者 -->
        <div v-if="skill.authorNickname" class="detail-author">
          <el-icon><User /></el-icon>
          分享者：{{ skill.authorNickname }}
        </div>
      </div>

      <!-- 适用产品 -->
      <div v-if="skill.applicable" class="applicable-section">
        <span class="section-label">适用产品</span>
        <div class="applicable-row">
          <span v-for="app in parseApplicable(skill.applicable)" :key="app" class="applicable-badge">
            {{ app }}
          </span>
        </div>
      </div>

      <!-- 提示词内容 -->
      <div class="prompt-section">
        <div class="prompt-header">
          <span class="section-label">提示词内容</span>
          <button class="copy-btn" @click="copyPrompt" :class="{ copied: justCopied }">
            <el-icon><CopyDocument v-if="!justCopied" /><Select v-else /></el-icon>
            {{ justCopied ? '已复制' : '复制' }}
          </button>
        </div>
        <pre class="prompt-content">{{ skill.prompt }}</pre>
      </div>

      <!-- 标签 -->
      <div v-if="skill.tags" class="detail-tags">
        <span v-for="tag in parseTags(skill.tags)" :key="tag" class="tag-chip"># {{ tag }}</span>
      </div>

      <!-- 互动区域 -->
      <div class="interact-section">
        <button class="interact-btn" :class="{ active: skill.liked }" @click="handleLike">
          <el-icon>
            <StarFilled v-if="skill.liked" />
            <Star v-else />
          </el-icon>
          <span class="interact-count">{{ skill.likeCount }}</span>
          <span class="interact-label">{{ skill.liked ? '已点赞' : '点赞' }}</span>
        </button>
        <button class="interact-btn collect-btn" :class="{ active: skill.collected }" @click="handleCollect">
          <el-icon>
            <CollectionTag v-if="skill.collected" />
            <Collection v-else />
          </el-icon>
          <span class="interact-count">{{ skill.collectCount }}</span>
          <span class="interact-label">{{ skill.collected ? '已收藏' : '收藏' }}</span>
        </button>
      </div>

      <!-- 相关技能 -->
      <div v-if="relatedSkills.length" class="related-section">
        <h2 class="related-title">同类技能</h2>
        <div class="related-grid">
          <router-link
            v-for="item in relatedSkills"
            :key="item.id"
            :to="`/skills/${item.id}`"
            class="related-card app-card"
          >
            <span
              class="cat-badge-sm"
              :style="{ background: getCategoryBg(item.category), color: getCategoryColor(item.category) }"
            >{{ item.category }}</span>
            <p class="related-card-title">{{ item.title }}</p>
            <p class="related-card-preview">{{ item.prompt }}</p>
            <div class="related-card-meta">
              <span><el-icon><Star /></el-icon>{{ item.likeCount }}</span>
              <span><el-icon><Collection /></el-icon>{{ item.collectCount }}</span>
            </div>
          </router-link>
        </div>
      </div>
    </template>

    <!-- 404 -->
    <div v-else class="not-found">技能不存在或已下线</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, View, User, StarFilled, Star, Collection, CollectionTag,
  CopyDocument, Select
} from '@element-plus/icons-vue'
import { getSkillDetail, getSkillList, likeSkill, collectSkill } from '@/api/skill'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/format'
import { SkillCategoryColor } from '@/types'
import type { SkillItem } from '@/types'

const route = useRoute()
const authStore = useAuthStore()

const skill = ref<SkillItem | null>(null)
const relatedSkills = ref<SkillItem[]>([])
const loading = ref(false)
const justCopied = ref(false)

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  skill.value = null
  relatedSkills.value = []
  try {
    skill.value = await getSkillDetail(id)
    // 加载同类技能（同分类，排除自身，最多3条）
    const res = await getSkillList({ category: skill.value.category, size: 4 })
    relatedSkills.value = res.records.filter(s => s.id !== id).slice(0, 3)
  } catch {
    // 错误由拦截器统一提示
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!authStore.isLoggedIn) { ElMessage.warning('请先登录后再点赞'); return }
  if (!skill.value) return
  try {
    const res = await likeSkill(skill.value.id)
    skill.value.liked = res.active
    skill.value.likeCount = res.count
    ElMessage.success(res.active ? '点赞成功' : '已取消点赞')
  } catch { /* 拦截器处理 */ }
}

async function handleCollect() {
  if (!authStore.isLoggedIn) { ElMessage.warning('请先登录后再收藏'); return }
  if (!skill.value) return
  try {
    const res = await collectSkill(skill.value.id)
    skill.value.collected = res.active
    skill.value.collectCount = res.count
    ElMessage.success(res.active ? '收藏成功' : '已取消收藏')
  } catch { /* 拦截器处理 */ }
}

async function copyPrompt() {
  if (!skill.value) return
  try {
    await navigator.clipboard.writeText(skill.value.prompt)
    justCopied.value = true
    setTimeout(() => { justCopied.value = false }, 2000)
  } catch {
    ElMessage.error('复制失败，请手动选择文本')
  }
}

function getCategoryColor(cat: string) {
  return SkillCategoryColor[cat] ?? '#6a7282'
}
function getCategoryBg(cat: string) {
  return getCategoryColor(cat) + '20'
}
function parseTags(tags: string) {
  return tags.split(',').map(t => t.trim()).filter(Boolean)
}
function parseApplicable(applicable: string) {
  return applicable.split(',').map(a => a.trim()).filter(Boolean)
}

onMounted(loadDetail)
// 从相关技能跳转时重新加载
watch(() => route.params.id, loadDetail)
</script>

<style scoped>
.skill-detail-page {
  padding-top: 28px;
  padding-bottom: 80px;
  max-width: 800px;
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
.meta-time, .meta-stat {
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

/* 适用产品 */
.applicable-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.section-label {
  font-size: 13px;
  color: var(--color-text-muted);
  font-weight: 600;
  flex-shrink: 0;
}
.applicable-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.applicable-badge {
  font-size: 12px;
  color: var(--color-primary);
  background: var(--color-primary-dim);
  border: 1px solid var(--color-border-hover);
  padding: 3px 12px;
  border-radius: var(--radius-sm);
}

/* 提示词区域 */
.prompt-section {
  margin-bottom: 20px;
}
.prompt-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.copy-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border-hover);
  background: transparent;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.copy-btn:hover { background: var(--color-primary-dim); }
.copy-btn.copied {
  background: var(--color-primary-dim);
  border-color: var(--color-primary);
}
.prompt-content {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  color: var(--color-text-primary);
  line-height: 1.7;
  background: var(--color-bg-overlay);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}

/* 标签 */
.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 28px;
}
.tag-chip {
  font-size: 12px;
  color: var(--color-text-disabled);
  background: var(--color-bg-overlay);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
}

/* 互动区域 */
.interact-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 24px 0 32px;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 40px;
}
.interact-btn {
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
.interact-btn:hover {
  border-color: var(--color-border-hover);
  color: var(--color-primary);
}
.interact-btn.active {
  background: var(--color-primary-dim);
  border-color: var(--color-border-hover);
  color: var(--color-primary);
}
.collect-btn.active {
  background: rgba(167, 139, 250, 0.12);
  border-color: rgba(167, 139, 250, 0.4);
  color: #a78bfa;
}
.collect-btn:hover {
  color: #a78bfa;
}
.interact-count {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 16px;
}
.interact-label { font-size: 13px; }

/* 相关技能 */
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
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.related-card-preview {
  font-size: 11px;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: monospace;
}
.related-card-meta {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--color-text-disabled);
}
.related-card-meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.not-found {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-muted);
}

@media (max-width: 600px) {
  .detail-title { font-size: 20px; }
  .related-grid { grid-template-columns: 1fr; }
  .interact-section { gap: 10px; }
  .interact-btn { padding: 10px 20px; }
}
</style>
