<template>
  <div class="library-page page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h1 class="page-title">AI 知识库</h1>
      <p class="page-sub">精选优质内容，直达原文 · 策展导航，不创作</p>
      <el-button
        type="primary"
        class="btn-submit"
        @click="submitDialogVisible = true"
      >+ 投稿文章  <span class="btn-reward">+10 积分</span></el-button>
    </div>

    <!-- 分类 Tabs -->
    <div class="filter-row">
      <div class="category-tabs">
        <span
          v-for="(label, val) in ResourceCategoryMap"
          :key="val"
          class="cat-tab"
          :class="{ active: activeCategory === Number(val) }"
          @click="setCategory(Number(val))"
        >{{ label }}</span>
      </div>
      <!-- 难度筛选 -->
      <div class="difficulty-chips">
        <span
          v-for="(label, val) in difficultyOptions"
          :key="val"
          class="diff-chip"
          :class="{ active: activeDifficulty === Number(val) }"
          @click="setDifficulty(Number(val))"
        >{{ label }}</span>
      </div>
    </div>

    <!-- 卡片网格 -->
    <div v-if="loading" class="resource-grid">
      <div v-for="i in 9" :key="i" class="resource-card app-card skeleton-card">
        <div class="skeleton" style="width:56px;height:16px;border-radius:4px;margin-bottom:12px"></div>
        <div class="skeleton" style="width:90%;height:15px;margin-bottom:6px"></div>
        <div class="skeleton" style="width:70%;height:15px;margin-bottom:12px"></div>
        <div class="skeleton" style="width:100%;height:12px;margin-bottom:5px"></div>
        <div class="skeleton" style="width:80%;height:12px;margin-bottom:16px"></div>
        <div class="skeleton" style="width:50%;height:11px"></div>
      </div>
    </div>

    <div v-else-if="resources.length" class="resource-grid">
      <div
        v-for="item in resources"
        :key="item.id"
        class="resource-card app-card"
        :style="{ '--cat-color': ResourceCategoryColor[item.category] ?? '#6a7282' }"
      >
        <!-- 顶部 meta -->
        <div class="rc-header">
          <span
            class="rc-cat-badge"
            :style="{ background: (ResourceCategoryColor[item.category] ?? '#6a7282') + '20', color: ResourceCategoryColor[item.category] ?? '#6a7282' }"
          >{{ item.categoryName }}</span>
          <span
            class="rc-diff-badge"
            :style="{ color: ResourceDifficultyColor[item.difficulty] ?? '#6a7282' }"
          >{{ item.difficultyName }}</span>
        </div>
        <!-- 标题 -->
        <p class="rc-title">{{ item.title }}</p>
        <!-- 摘要 -->
        <p v-if="item.summary" class="rc-summary">{{ item.summary }}</p>
        <!-- 底部 -->
        <div class="rc-footer">
          <span class="rc-author">{{ item.author || '编辑精选' }}</span>
          <div class="rc-actions">
            <span class="rc-stat">{{ item.viewCount }} 浏览</span>
            <span class="rc-stat">{{ item.collectCount }} 收藏</span>
            <el-button
              size="small"
              type="primary"
              plain
              class="rc-read-btn"
              @click="openSource(item)"
            >阅读原文 →</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无内容</p>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadResources"
      />
    </div>

    <!-- 投稿 Dialog -->
    <el-dialog v-model="submitDialogVisible" title="投稿优质文章" width="500px">
      <div class="submit-reward-hint">审核通过后将自动发布，并奖励 <b>+10 积分</b></div>
      <el-form :model="submitForm" label-position="top" size="small">
        <el-form-item label="文章标题 *">
          <el-input v-model="submitForm.title" placeholder="请输入原文标题" />
        </el-form-item>
        <el-form-item label="原文链接 *">
          <el-input v-model="submitForm.sourceUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="简短摘要（可选）">
          <el-input v-model="submitForm.summary" type="textarea" :rows="2" placeholder="一两句话描述文章内容" />
        </el-form-item>
        <el-form-item label="作者/来源">
          <el-input v-model="submitForm.author" placeholder="如：机器之心、Anthropic Blog" />
        </el-form-item>
        <el-form-item label="分类 *">
          <el-select v-model="submitForm.category" placeholder="选择分类">
            <template v-for="(label, val) in ResourceCategoryMap" :key="val">
              <el-option v-if="Number(val) > 0" :label="label" :value="Number(val)" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="submitForm.difficulty" placeholder="选择难度">
            <el-option label="入门" :value="1" />
            <el-option label="进阶" :value="2" />
            <el-option label="专业" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getResourceList, collectResource, submitResource } from '@/api/resource'
import {
  ResourceCategoryMap, ResourceCategoryColor,
  ResourceDifficultyColor, ResourceDifficultyMap
} from '@/types'
import type { ResourceItem } from '@/types'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const difficultyOptions = { 0: '全部', ...ResourceDifficultyMap }

const resources = ref<ResourceItem[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const activeCategory = ref(0)
const activeDifficulty = ref(0)

const submitDialogVisible = ref(false)
const submitting = ref(false)
const submitForm = reactive({
  title: '',
  sourceUrl: '',
  summary: '',
  author: '',
  category: undefined as number | undefined,
  difficulty: 1,
})

async function loadResources(page = 1) {
  currentPage.value = page
  loading.value = true
  try {
    const res = await getResourceList({
      category: activeCategory.value || undefined,
      difficulty: activeDifficulty.value || undefined,
      page,
      size: pageSize,
    })
    resources.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function setCategory(val: number) {
  activeCategory.value = val
  loadResources(1)
}

function setDifficulty(val: number) {
  activeDifficulty.value = val
  loadResources(1)
}

function openSource(item: ResourceItem) {
  if (item.sourceUrl) {
    window.open(item.sourceUrl, '_blank', 'noopener,noreferrer')
  }
}

async function toggleCollect(item: ResourceItem) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  const res = await collectResource(item.id)
  item.collected = res.active
  item.collectCount = res.count
}

async function handleSubmit() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!submitForm.title.trim()) { ElMessage.warning('请填写标题'); return }
  if (!submitForm.sourceUrl.trim()) { ElMessage.warning('请填写原文链接'); return }
  if (!submitForm.category) { ElMessage.warning('请选择分类'); return }
  submitting.value = true
  try {
    await submitResource({
      title: submitForm.title,
      sourceUrl: submitForm.sourceUrl,
      summary: submitForm.summary || undefined,
      author: submitForm.author || undefined,
      category: submitForm.category,
      difficulty: submitForm.difficulty,
    })
    ElMessage.success('投稿成功！审核通过后将发布并奖励 +10 积分')
    submitDialogVisible.value = false
    submitForm.title = ''
    submitForm.sourceUrl = ''
    submitForm.summary = ''
    submitForm.author = ''
    submitForm.category = undefined
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadResources())
</script>

<style scoped>
.library-page {
  padding: 32px 0 60px;
}
.page-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.page-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}
.page-sub {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
  flex: 1;
}
.btn-submit {
  margin-left: auto;
  font-weight: 600;
}
.btn-reward {
  font-size: 11px;
  font-weight: 700;
  background: rgba(255,255,255,0.18);
  border-radius: 4px;
  padding: 1px 5px;
  margin-left: 4px;
}
.submit-reward-hint {
  font-size: 13px;
  color: var(--color-text-muted);
  background: rgba(0, 210, 239, 0.06);
  border: 1px solid rgba(0, 210, 239, 0.15);
  border-radius: var(--radius-md);
  padding: 8px 14px;
  margin-bottom: 16px;
  line-height: 1.6;
}
.submit-reward-hint b {
  color: var(--color-primary);
}
.filter-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
}
.category-tabs, .difficulty-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.cat-tab, .diff-chip {
  padding: 5px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.cat-tab:hover, .cat-tab.active,
.diff-chip:hover, .diff-chip.active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
  border-color: var(--color-border-hover);
}
.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.resource-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 0;
  border-top: 2px solid var(--cat-color, var(--color-border));
  transition: border-color 0.2s;
}
.resource-card:hover {
  border-top-color: var(--cat-color, var(--color-primary));
}
.rc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.rc-cat-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.rc-diff-badge {
  font-size: 10px;
  font-weight: 500;
}
.rc-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.5;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.rc-summary {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0 0 12px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.rc-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--color-border);
}
.rc-author {
  font-size: 11px;
  color: var(--color-text-disabled);
  flex-shrink: 0;
}
.rc-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.rc-stat {
  font-size: 11px;
  color: var(--color-text-disabled);
}
.rc-read-btn {
  font-size: 12px;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-muted);
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
@media (max-width: 1024px) {
  .resource-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .resource-grid { grid-template-columns: 1fr; }
}
</style>
