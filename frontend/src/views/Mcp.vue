<template>
  <div class="mcp-page page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h1 class="page-title">Agent 技能库</h1>
      <p class="page-sub">MCP Server 目录 &amp; Agent Skill 工具 · 让 AI 连接真实世界</p>
    </div>

    <!-- 类型 Filter -->
    <div class="filter-row">
      <div class="type-tabs">
        <span
          v-for="tab in typeTabs"
          :key="tab.value"
          class="type-tab"
          :class="{ active: activeType === tab.value }"
          @click="setType(tab.value)"
        >{{ tab.label }}</span>
      </div>
      <div class="category-chips">
        <span
          v-for="cat in allCategories"
          :key="cat"
          class="cat-chip"
          :class="{ active: activeCategory === cat }"
          @click="setCategory(cat)"
        >{{ cat }}</span>
      </div>
    </div>

    <!-- 卡片列表 -->
    <div v-if="loading" class="mcp-grid">
      <div v-for="i in 6" :key="i" class="mcp-card app-card skeleton-card">
        <div class="skeleton" style="width:64px;height:18px;border-radius:4px;margin-bottom:12px"></div>
        <div class="skeleton" style="width:50%;height:16px;margin-bottom:8px"></div>
        <div class="skeleton" style="width:100%;height:12px;margin-bottom:5px"></div>
        <div class="skeleton" style="width:85%;height:12px;margin-bottom:16px"></div>
        <div class="skeleton" style="width:40%;height:12px"></div>
      </div>
    </div>

    <div v-else-if="items.length" class="mcp-grid">
      <div
        v-for="item in items"
        :key="item.id"
        class="mcp-card app-card"
        :style="{ '--type-color': McpTypeColor[item.type] ?? '#6a7282' }"
      >
        <div class="mc-header">
          <span
            class="mc-type-badge"
            :style="{ background: (McpTypeColor[item.type] ?? '#6a7282') + '20', color: McpTypeColor[item.type] ?? '#6a7282' }"
          >{{ item.typeName }}</span>
          <span v-if="item.category" class="mc-cat">{{ item.category }}</span>
        </div>
        <p class="mc-name">{{ item.name }}</p>
        <p class="mc-desc">{{ item.description }}</p>
        <div v-if="item.vendor" class="mc-vendor">by {{ item.vendor }}</div>
        <div class="mc-footer">
          <div class="mc-stats">
            <span>{{ item.useCount }} 使用</span>
            <span>{{ item.collectCount }} 收藏</span>
          </div>
          <div class="mc-btns">
            <el-button
              v-if="item.installGuide"
              size="small"
              plain
              @click="showGuide(item)"
            >安装指南</el-button>
            <el-button
              v-if="item.sourceUrl"
              size="small"
              type="primary"
              plain
              @click="openSource(item)"
            >查看源码</el-button>
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
        @current-change="loadItems"
      />
    </div>

    <!-- 安装指南 Dialog -->
    <el-dialog v-model="guideVisible" :title="guideItem?.name + ' 安装指南'" width="600px">
      <div class="guide-content" v-html="renderedGuide"></div>
      <template #footer>
        <el-button @click="guideVisible = false">关闭</el-button>
        <el-button v-if="guideItem?.sourceUrl" type="primary" @click="openSource(guideItem!)">查看源码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMcpList, recordMcpUse } from '@/api/mcp'
import { McpTypeColor } from '@/types'
import type { McpSkillItem } from '@/types'

const typeTabs = [
  { value: 0, label: '全部' },
  { value: 1, label: 'MCP Server' },
  { value: 2, label: 'Agent Skill' },
]

const allCategories = ['文件管理', '浏览器', '代码开发', '通讯协作', '研究分析', '数据库', '其他']

const items = ref<McpSkillItem[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const activeType = ref(0)
const activeCategory = ref('')

const guideVisible = ref(false)
const guideItem = ref<McpSkillItem | null>(null)

// 简单 markdown → html（仅处理代码块和换行）
const renderedGuide = computed(() => {
  if (!guideItem.value?.installGuide) return ''
  return guideItem.value.installGuide
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/\n/g, '<br>')
})

async function loadItems(page = 1) {
  currentPage.value = page
  loading.value = true
  try {
    const res = await getMcpList({
      type: activeType.value || undefined,
      category: activeCategory.value || undefined,
      page,
      size: pageSize,
    })
    items.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function setType(val: number) {
  activeType.value = val
  loadItems(1)
}

function setCategory(cat: string) {
  activeCategory.value = activeCategory.value === cat ? '' : cat
  loadItems(1)
}

function openSource(item: McpSkillItem) {
  if (item.sourceUrl) {
    recordMcpUse(item.id)
    window.open(item.sourceUrl, '_blank', 'noopener,noreferrer')
  }
}

function showGuide(item: McpSkillItem) {
  guideItem.value = item
  guideVisible.value = true
}

onMounted(() => loadItems())
</script>

<style scoped>
.mcp-page {
  padding: 32px 0 60px;
}
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 6px;
}
.page-sub {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
}
.filter-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
}
.type-tabs, .category-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.type-tab, .cat-chip {
  padding: 5px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.type-tab:hover, .type-tab.active,
.cat-chip:hover, .cat-chip.active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
  border-color: var(--color-border-hover);
}
.mcp-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.mcp-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  border-left: 3px solid var(--type-color, var(--color-border));
  transition: border-color 0.2s;
}
.mcp-card:hover {
  border-left-color: var(--type-color, var(--color-primary));
}
.mc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.mc-type-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.mc-cat {
  font-size: 11px;
  color: var(--color-text-disabled);
}
.mc-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}
.mc-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.55;
  margin: 0 0 8px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.mc-vendor {
  font-size: 11px;
  color: var(--color-text-disabled);
  margin-bottom: 12px;
}
.mc-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--color-border);
}
.mc-stats {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--color-text-disabled);
}
.mc-btns {
  display: flex;
  gap: 6px;
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
.guide-content {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.7;
}
.guide-content :deep(pre) {
  background: var(--color-bg-overlay);
  padding: 12px 16px;
  border-radius: var(--radius-md);
  overflow-x: auto;
  font-size: 12px;
  margin: 10px 0;
}
.guide-content :deep(code) {
  font-family: monospace;
  color: var(--color-primary);
}
/* Dialog 暗色主题 */
:deep(.el-dialog) {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}
:deep(.el-dialog__header) {
  background: transparent;
  border-bottom: 1px solid var(--color-border);
  padding: 16px 20px;
  margin: 0;
}
:deep(.el-dialog__title) {
  color: var(--color-text-primary);
  font-size: 15px;
  font-weight: 600;
}
:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--color-text-muted);
}
:deep(.el-dialog__body) {
  background: transparent;
  color: var(--color-text-muted);
  padding: 20px;
}
:deep(.el-dialog__footer) {
  background: transparent;
  border-top: 1px solid var(--color-border);
  padding: 12px 20px;
}
@media (max-width: 1024px) {
  .mcp-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .mcp-grid { grid-template-columns: 1fr; }
}
</style>
