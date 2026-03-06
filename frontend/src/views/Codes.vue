<template>
  <div class="codes-page">
    <!-- Hero -->
    <section class="hero">
      <div class="page-container">
        <h1 class="hero-title">AI 邀请码接龙</h1>
        <p class="hero-sub">社区共享 · 积分激励 · 接龙裂变</p>
        <div class="group-tabs">
          <span
            v-for="tab in tabs"
            :key="tab.value"
            class="group-tab"
            :class="{ active: activeGroup === tab.value }"
            @click="switchGroup(tab.value)"
          >{{ tab.label }}</span>
        </div>
      </div>
    </section>

    <!-- 产品网格 -->
    <section class="products-section page-container">
      <div v-if="loading" class="products-grid">
        <div v-for="i in 8" :key="i" class="app-card product-card skeleton-card">
          <div class="skeleton" style="width:48px;height:48px;border-radius:12px;margin-bottom:12px"></div>
          <div class="skeleton" style="width:60%;height:18px;margin-bottom:8px"></div>
          <div class="skeleton" style="width:40%;height:14px;margin-bottom:12px"></div>
          <div class="skeleton" style="width:90%;height:12px;margin-bottom:6px"></div>
          <div class="skeleton" style="width:75%;height:12px"></div>
        </div>
      </div>

      <div v-else-if="products.length" class="products-grid">
        <router-link
          v-for="p in products"
          :key="p.id"
          :to="`/product/${p.id}`"
          class="app-card product-card"
          :class="{ 'openclaw-card': p.productGroup === 1 }"
        >
          <div class="product-logo" :class="{ 'openclaw-logo': p.productGroup === 1 }">{{ p.name[0] }}</div>
          <div class="product-name">{{ p.name }}</div>
          <div class="product-meta">
            <span v-if="p.productGroup === 1" class="group-badge openclaw">OpenClaw 生态</span>
            <span v-else-if="p.productGroup === 3" class="group-badge other">其他</span>
            <span v-else class="category-tag">{{ CategoryMap[p.category] ?? '其他' }}</span>
            <span class="code-count" :class="{ available: p.codeCount > 0 }">
              {{ p.codeCount > 0 ? `${p.codeCount} 码可用` : '暂无邀请码' }}
            </span>
          </div>
          <p class="product-desc">{{ p.description }}</p>
        </router-link>
      </div>

      <div v-else class="empty-state">
        <p>该分组下暂无产品</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProducts } from '@/api/product'
import { CategoryMap } from '@/types'

const tabs = [
  { value: 0, label: '全部' },
  { value: 1, label: 'OpenClaw 生态' },
  { value: 2, label: 'AI 应用' },
  { value: 3, label: '其他' },
]

const activeGroup = ref(0)
const products = ref<any[]>([])
const loading = ref(false)

async function loadProducts(group: number) {
  loading.value = true
  try {
    products.value = await getProducts(group ? { group } : {})
  } finally {
    loading.value = false
  }
}

function switchGroup(val: number) {
  activeGroup.value = val
  loadProducts(val)
}

onMounted(() => {
  loadProducts(0)
})
</script>

<style scoped>
.hero {
  padding: 48px 0 32px;
  text-align: center;
}
.hero-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  letter-spacing: 0.02em;
  background: linear-gradient(135deg, #f0f2f5 30%, var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-sub {
  font-size: 15px;
  color: var(--color-text-muted);
  margin-bottom: 28px;
}
.group-tabs {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
}
.group-tab {
  padding: 6px 20px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.group-tab:hover,
.group-tab.active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
  border-color: var(--color-border-hover);
}
.products-section {
  padding-bottom: 60px;
}
.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.product-card {
  padding: 20px;
  text-decoration: none;
  display: block;
  cursor: pointer;
}
.openclaw-card {
  border-color: rgba(0, 210, 239, 0.25);
}
.openclaw-card:hover {
  border-color: var(--color-primary);
}
.skeleton-card { cursor: default; }
.product-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--color-primary-dim);
  color: var(--color-primary);
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}
.openclaw-logo {
  background: rgba(0, 210, 239, 0.18);
  box-shadow: 0 0 12px rgba(0, 210, 239, 0.2);
}
.product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.category-tag {
  font-size: 11px;
  color: var(--color-text-muted);
  background: var(--color-bg-overlay);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.group-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.group-badge.openclaw {
  background: rgba(0, 210, 239, 0.12);
  color: var(--color-primary);
}
.group-badge.other {
  background: var(--color-bg-overlay);
  color: var(--color-text-muted);
}
.code-count {
  font-size: 11px;
  color: var(--color-text-disabled);
}
.code-count.available {
  color: var(--color-success);
}
.product-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-muted);
}
@media (max-width: 1024px) {
  .products-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .products-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-title { font-size: 24px; }
}
</style>
