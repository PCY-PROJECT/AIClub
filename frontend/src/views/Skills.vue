<template>
  <div class="skills-page">
    <!-- Hero -->
    <section class="skills-hero">
      <div class="page-container">
        <h1 class="skills-hero-title">AI 技能广场</h1>
        <p class="skills-hero-sub">探索社区精选提示词，解锁 AI 的无限潜能</p>
      </div>
    </section>

    <div class="page-container skills-body">
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
      <div v-if="loading" class="skills-grid">
        <div v-for="i in 6" :key="i" class="skill-card app-card skeleton-card">
          <div class="skeleton" style="width:64px;height:22px;border-radius:6px;margin-bottom:12px"></div>
          <div class="skeleton" style="width:85%;height:18px;margin-bottom:10px"></div>
          <div class="skeleton" style="width:100%;height:14px;margin-bottom:6px"></div>
          <div class="skeleton" style="width:90%;height:14px;margin-bottom:6px"></div>
          <div class="skeleton" style="width:60%;height:14px;margin-bottom:16px"></div>
          <div class="skeleton" style="width:50%;height:12px"></div>
        </div>
      </div>

      <!-- 技能卡片列表 -->
      <div v-else-if="skillList.length" class="skills-grid">
        <router-link
          v-for="item in skillList"
          :key="item.id"
          :to="`/skills/${item.id}`"
          class="skill-card app-card"
          :style="{ '--cat-color': getCategoryColor(item.category) }"
        >
          <!-- 分类标签 + 时间 -->
          <div class="skill-card-top">
            <span class="cat-badge" :style="{ background: getCategoryBg(item.category), color: getCategoryColor(item.category) }">
              {{ item.category }}
            </span>
            <span class="skill-time">{{ formatRelativeTime(item.createTime) }}</span>
          </div>

          <!-- 标题 -->
          <h3 class="skill-title">{{ item.title }}</h3>

          <!-- 提示词预览 -->
          <p class="skill-prompt-preview">{{ item.prompt }}</p>

          <!-- 适用产品 -->
          <div v-if="item.applicable" class="applicable-row">
            <span v-for="app in parseApplicable(item.applicable)" :key="app" class="applicable-badge">
              {{ app }}
            </span>
          </div>

          <!-- 标签 -->
          <div v-if="item.tags" class="tags-row">
            <span v-for="tag in parseTags(item.tags)" :key="tag" class="tag-chip"># {{ tag }}</span>
          </div>

          <!-- 底部元信息 -->
          <div class="skill-card-footer">
            <span class="meta-item">
              <el-icon><View /></el-icon>{{ item.useCount }}
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>{{ item.likeCount }}
            </span>
            <span class="meta-item">
              <el-icon><Collection /></el-icon>{{ item.collectCount }}
            </span>
            <span v-if="item.authorNickname" class="meta-item author">
              <el-icon><User /></el-icon>{{ item.authorNickname }}
            </span>
          </div>
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">暂无相关技能</div>

      <!-- 加载更多 -->
      <div v-if="!loading && hasMore" class="load-more-wrap">
        <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
      </div>
    </div>

    <!-- 投稿按钮（已登录可见） -->
    <div v-if="authStore.isLoggedIn" class="fab-submit-wrap">
      <button class="fab-submit" @click="submitVisible = true" title="分享技能">
        <el-icon><Plus /></el-icon>
        <span class="fab-text">分享提示词</span>
      </button>
      <div class="fab-submit-hint">审核通过 +15 积分</div>
    </div>

    <!-- 投稿对话框 -->
    <el-dialog v-model="submitVisible" title="分享技能提示词" width="560px" :close-on-click-modal="false">
      <div class="submit-form">
        <div class="form-item">
          <label class="form-label">标题 <span class="required">*</span></label>
          <el-input v-model="form.title" placeholder="技能标题，简明描述用途" maxlength="60" show-word-limit />
        </div>
        <div class="form-item">
          <label class="form-label">分类 <span class="required">*</span></label>
          <el-select v-model="form.category" placeholder="选择技能分类" style="width:100%">
            <el-option v-for="cat in skillCategories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </div>
        <div class="form-item">
          <label class="form-label">提示词内容 <span class="required">*</span></label>
          <el-input
            v-model="form.prompt"
            type="textarea"
            :rows="5"
            placeholder="输入完整的提示词内容，越详细越好"
            maxlength="2000"
            show-word-limit
          />
        </div>
        <div class="form-item">
          <label class="form-label">适用产品 <span class="form-hint">多个产品用逗号分隔，如：ChatGPT,Claude</span></label>
          <el-input v-model="form.applicable" placeholder="可选，如：ChatGPT,Claude,Gemini" />
        </div>
        <div class="form-item">
          <label class="form-label">标签 <span class="form-hint">用逗号分隔，如：创意写作,故事</span></label>
          <el-input v-model="form.tags" placeholder="可选，最多3个标签" />
        </div>
      </div>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交分享
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View, Star, Collection, User, Plus } from '@element-plus/icons-vue'
import { getSkillList, submitSkill } from '@/api/skill'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/format'
import { SkillCategoryColor } from '@/types'
import type { SkillItem } from '@/types'

const authStore = useAuthStore()

// ─── 分类 ─────────────────────────────────────────────────
const skillCategories = ['写作', '编程', '设计', '营销', '分析', '翻译', '教育', '职场', '生活']
const categoryTabs = [
  { value: '', label: '全部' },
  ...skillCategories.map(c => ({ value: c, label: c })),
]

// ─── 列表状态 ─────────────────────────────────────────────
const activeCategory = ref('')
const skillList = ref<SkillItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const hasMore = ref(false)
const PAGE_SIZE = 12

async function loadSkills(reset = true) {
  if (reset) {
    loading.value = true
    currentPage.value = 1
    skillList.value = []
  } else {
    loadingMore.value = true
  }
  try {
    const res = await getSkillList({
      category: activeCategory.value || undefined,
      page: currentPage.value,
      size: PAGE_SIZE,
    })
    skillList.value = reset ? res.records : [...skillList.value, ...res.records]
    hasMore.value = skillList.value.length < res.total
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function switchCategory(val: string) {
  activeCategory.value = val
  loadSkills(true)
}

async function loadMore() {
  currentPage.value++
  await loadSkills(false)
}

// ─── 投稿 ────────────────────────────────────────────────
const submitVisible = ref(false)
const submitting = ref(false)
const form = ref({ title: '', category: '写作', prompt: '', applicable: '', tags: '' })

async function handleSubmit() {
  if (!form.value.title.trim()) { ElMessage.warning('请填写标题'); return }
  if (!form.value.prompt.trim()) { ElMessage.warning('请填写提示词内容'); return }
  submitting.value = true
  try {
    await submitSkill({
      title: form.value.title.trim(),
      prompt: form.value.prompt.trim(),
      category: form.value.category,
      applicable: form.value.applicable.trim() || undefined,
      tags: form.value.tags.trim() || undefined,
    })
    ElMessage.success('提交成功！审核通过后将发布并奖励 +15 积分')
    submitVisible.value = false
    form.value = { title: '', category: '写作', prompt: '', applicable: '', tags: '' }
  } finally {
    submitting.value = false
  }
}

// ─── 工具 ────────────────────────────────────────────────
function getCategoryColor(cat: string) {
  return SkillCategoryColor[cat] ?? '#6a7282'
}
function getCategoryBg(cat: string) {
  return getCategoryColor(cat) + '20'
}
function parseTags(tags: string) {
  return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 3)
}
function parseApplicable(applicable: string) {
  return applicable.split(',').map(a => a.trim()).filter(Boolean).slice(0, 4)
}

onMounted(() => loadSkills())
</script>

<style scoped>
.skills-hero {
  padding: 40px 0 28px;
  text-align: center;
}
.skills-hero-title {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #f0f2f5 30%, var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.skills-hero-sub {
  font-size: 14px;
  color: var(--color-text-muted);
}
.skills-body {
  padding-bottom: 80px;
}

/* 分类 Tab */
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

/* 技能网格 */
.skills-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

/* 技能卡片 */
.skill-card {
  display: block;
  padding: 20px;
  text-decoration: none;
  cursor: pointer;
  border-top: 2px solid var(--cat-color, var(--color-border));
  transition: border-color 0.2s, box-shadow 0.2s;
}
.skill-card:hover {
  border-top-color: var(--cat-color, var(--color-primary));
}
.skeleton-card { cursor: default; border-top-color: var(--color-border) !important; }

.skill-card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.cat-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.skill-time {
  font-size: 12px;
  color: var(--color-text-disabled);
}
.skill-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.skill-prompt-preview {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: monospace;
  background: var(--color-bg-overlay);
  padding: 8px 10px;
  border-radius: var(--radius-sm);
}
.applicable-row {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.applicable-badge {
  font-size: 10px;
  color: var(--color-primary);
  background: var(--color-primary-dim);
  border: 1px solid var(--color-border-hover);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.tags-row {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.tag-chip {
  font-size: 11px;
  color: var(--color-text-disabled);
  background: var(--color-bg-overlay);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.skill-card-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--color-border);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-disabled);
}
.meta-item.author { color: var(--color-text-muted); margin-left: auto; }

.load-more-wrap {
  text-align: center;
  margin-top: 28px;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

/* FAB */
.fab-submit-wrap {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.fab-submit-hint {
  font-size: 11px;
  color: var(--color-primary);
  background: rgba(0, 210, 239, 0.1);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  white-space: nowrap;
  font-weight: 500;
}
.fab-submit {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 40px;
  border-radius: 20px;
  background: var(--color-primary);
  color: #09090f;
  border: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 16px var(--color-primary-glow);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  border: none;
  outline: none;
}
.fab-text {
  font-size: 13px;
  font-weight: 600;
}
.fab-submit:hover {
  transform: scale(1.05);
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

@media (max-width: 1024px) {
  .skills-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .skills-grid { grid-template-columns: 1fr; }
  .fab-submit-wrap { bottom: 20px; right: 16px; }
}
</style>
