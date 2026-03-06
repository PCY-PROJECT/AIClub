<template>
  <div class="sites-page">
    <!-- Hero -->
    <section class="hero">
      <div class="page-container">
        <h1 class="hero-title">AI 网站导航</h1>
        <p class="hero-sub">精选优质 AI 工具与资源，由社区共同维护</p>
        <el-button type="primary" size="small" class="btn-submit" @click="showSubmit = true">
          + 推荐网站
        </el-button>
      </div>
    </section>

    <!-- 分类筛选 -->
    <div class="category-bar page-container">
      <span
        v-for="cat in ['全部', ...SiteCategoryList]"
        :key="cat"
        class="cat-tab"
        :class="{ active: activeCategory === cat }"
        @click="switchCategory(cat)"
      >{{ cat }}</span>
    </div>

    <!-- 网站网格 -->
    <section class="sites-section page-container">
      <div v-if="loading" class="sites-grid">
        <div v-for="i in 9" :key="i" class="app-card site-card skeleton-card">
          <div class="skeleton" style="width:40px;height:40px;border-radius:10px;margin-bottom:12px"></div>
          <div class="skeleton" style="width:70%;height:16px;margin-bottom:8px"></div>
          <div class="skeleton" style="width:100%;height:12px;margin-bottom:4px"></div>
          <div class="skeleton" style="width:85%;height:12px;margin-bottom:12px"></div>
          <div class="skeleton" style="width:40%;height:11px"></div>
        </div>
      </div>

      <div v-else-if="sites.length" class="sites-grid">
        <a
          v-for="site in sites"
          :key="site.id"
          :href="site.url"
          target="_blank"
          rel="noopener noreferrer"
          class="app-card site-card"
          :style="{ '--cat-color': SiteCategoryColor[site.category ?? ''] ?? '#6a7282' }"
          @click="recordView(site.id)"
        >
          <div class="site-logo">
            <img v-if="site.logo" :src="site.logo" :alt="site.name" @error="(e: any) => e.target.style.display='none'" />
            <span v-else class="logo-fallback">{{ site.name[0] }}</span>
          </div>
          <div class="site-name">{{ site.name }}</div>
          <p class="site-desc">{{ site.description }}</p>
          <div class="site-footer">
            <span class="site-cat-badge" :style="{ background: (SiteCategoryColor[site.category ?? ''] ?? '#6a7282') + '18', color: SiteCategoryColor[site.category ?? ''] ?? '#6a7282' }">
              {{ site.category ?? '其他' }}
            </span>
            <span class="site-views">{{ site.viewCount }} 访问</span>
          </div>
        </a>
      </div>

      <div v-else class="empty-state">
        <p>该分类下暂无网站</p>
      </div>
    </section>

    <!-- 投稿 Dialog -->
    <el-dialog v-model="showSubmit" title="推荐 AI 网站" width="480px" :close-on-click-modal="false">
      <el-form :model="form" label-position="top" size="default">
        <el-form-item label="网站名称 *">
          <el-input v-model="form.name" placeholder="例：Perplexity" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="网站链接 *">
          <el-input v-model="form.url" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="简介 *">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="一句话介绍这个网站的用途（200字内）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width:100%">
            <el-option v-for="c in SiteCategoryList" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签（逗号分隔）">
          <el-input v-model="form.tags" placeholder="例：AI,工具,搜索" maxlength="128" />
        </el-form-item>
        <el-form-item label="Logo URL（可选）">
          <el-input v-model="form.logo" placeholder="https://..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmit = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSiteList, submitSite, recordSiteView } from '@/api/site'
import { SiteCategoryList, SiteCategoryColor } from '@/types'
import type { SiteItem } from '@/types'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const sites = ref<SiteItem[]>([])
const loading = ref(false)
const activeCategory = ref('全部')
const showSubmit = ref(false)
const submitting = ref(false)

const form = ref({
  name: '',
  url: '',
  description: '',
  category: '',
  tags: '',
  logo: '',
})

async function loadSites(cat: string) {
  loading.value = true
  try {
    sites.value = await getSiteList(cat === '全部' ? undefined : cat)
  } finally {
    loading.value = false
  }
}

function switchCategory(cat: string) {
  activeCategory.value = cat
  loadSites(cat)
}

function recordView(id: number) {
  recordSiteView(id).catch(() => {})
}

async function handleSubmit() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录后再投稿')
    showSubmit.value = false
    return
  }
  if (!form.value.name || !form.value.url || !form.value.description) {
    ElMessage.warning('请填写名称、链接和简介')
    return
  }
  submitting.value = true
  try {
    await submitSite(form.value)
    ElMessage.success('投稿成功，等待审核后上线')
    showSubmit.value = false
    form.value = { name: '', url: '', description: '', category: '', tags: '', logo: '' }
  } catch {
    ElMessage.error('投稿失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadSites('全部'))
</script>

<style scoped>
.hero {
  padding: 48px 0 24px;
  text-align: center;
}
.hero-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #f0f2f5 30%, var(--color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-sub {
  font-size: 15px;
  color: var(--color-text-muted);
  margin-bottom: 18px;
}
.btn-submit {
  font-weight: 600;
}
.category-bar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding-top: 0;
  padding-bottom: 24px;
}
.cat-tab {
  padding: 5px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.cat-tab:hover,
.cat-tab.active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
  border-color: var(--color-border-hover);
}
.sites-section {
  padding-bottom: 60px;
}
.sites-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.site-card {
  padding: 20px;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  border-top: 2px solid var(--cat-color, var(--color-border));
  transition: border-color 0.2s;
}
.site-card:hover {
  border-top-color: var(--cat-color, var(--color-primary));
}
.skeleton-card { cursor: default; }
.site-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--color-bg-overlay);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  overflow: hidden;
  flex-shrink: 0;
}
.site-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.logo-fallback {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
  font-family: var(--font-display);
}
.site-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}
.site-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.55;
  flex: 1;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.site-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.site-cat-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
.site-views {
  font-size: 11px;
  color: var(--color-text-disabled);
}
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-muted);
}
@media (max-width: 1024px) {
  .sites-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .sites-grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 24px; }
}
</style>
