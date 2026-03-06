<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="page-container">
        <h1 class="hero-title">AI 入门者的第一站</h1>
        <p class="hero-sub">知识库 · 提示词广场 · Agent 技能库 · AI 工具导航</p>
      </div>
    </section>

    <!-- 知识库精选 -->
    <section class="preview-section page-container">
      <div class="section-header">
        <h2 class="section-title">知识库精选</h2>
        <router-link to="/library" class="section-more">查看全部 →</router-link>
      </div>
      <div class="resource-grid">
        <template v-if="resourceLoading">
          <div v-for="i in 3" :key="i" class="app-card resource-card skeleton-card">
            <div class="skeleton" style="width:60px;height:18px;border-radius:4px;margin-bottom:10px"></div>
            <div class="skeleton" style="width:90%;height:16px;margin-bottom:6px"></div>
            <div class="skeleton" style="width:70%;height:16px;margin-bottom:12px"></div>
            <div class="skeleton" style="width:45%;height:12px"></div>
          </div>
        </template>
        <a v-else v-for="item in resourcePreview" :key="item.id"
          :href="item.sourceUrl" target="_blank" rel="noopener noreferrer"
          class="app-card resource-card"
          :style="{ '--cat-color': ResourceCategoryColor[item.category] ?? '#6a7282' }">
          <span class="badge"
            :style="{ background: (ResourceCategoryColor[item.category] ?? '#6a7282') + '20', color: ResourceCategoryColor[item.category] ?? '#6a7282' }">
            {{ item.categoryName }}
          </span>
          <p class="card-title">{{ item.title }}</p>
          <div class="card-meta">
            <span>{{ item.author || '编辑精选' }}</span>
            <span>{{ item.viewCount }} 浏览</span>
          </div>
        </a>
      </div>
    </section>

    <!-- 热门提示词 -->
    <section class="preview-section page-container">
      <div class="section-header">
        <h2 class="section-title">热门提示词</h2>
        <router-link to="/skills" class="section-more">查看全部 →</router-link>
      </div>
      <div class="skill-grid">
        <template v-if="skillsLoading">
          <div v-for="i in 3" :key="i" class="app-card skill-card skeleton-card">
            <div class="skeleton" style="width:52px;height:18px;border-radius:4px;margin-bottom:10px"></div>
            <div class="skeleton" style="width:88%;height:15px;margin-bottom:8px"></div>
            <div class="skeleton" style="width:100%;height:12px;margin-bottom:5px"></div>
            <div class="skeleton" style="width:80%;height:12px;margin-bottom:10px"></div>
            <div class="skeleton" style="width:40%;height:11px"></div>
          </div>
        </template>
        <router-link v-else v-for="item in skillsPreview" :key="item.id"
          :to="`/skills/${item.id}`" class="app-card skill-card"
          :style="{ '--cat-color': SkillCategoryColor[item.category] ?? '#6a7282' }">
          <span class="badge"
            :style="{ background: (SkillCategoryColor[item.category] ?? '#6a7282') + '20', color: SkillCategoryColor[item.category] ?? '#6a7282' }">
            {{ item.category }}
          </span>
          <p class="card-title">{{ item.title }}</p>
          <p class="skill-prompt">{{ item.prompt }}</p>
          <div class="card-meta">
            <span>{{ item.likeCount }} 赞</span>
            <span>{{ item.collectCount }} 收藏</span>
            <span>{{ item.useCount }} 使用</span>
          </div>
        </router-link>
      </div>
    </section>

    <!-- Agent 技能 -->
    <section class="preview-section page-container">
      <div class="section-header">
        <h2 class="section-title">Agent 技能</h2>
        <router-link to="/mcp" class="section-more">查看全部 →</router-link>
      </div>
      <div class="agent-grid">
        <template v-if="mcpLoading">
          <div v-for="i in 4" :key="i" class="app-card agent-card skeleton-card">
            <div class="skeleton" style="width:80%;height:15px;margin-bottom:8px"></div>
            <div class="skeleton" style="width:100%;height:12px;margin-bottom:4px"></div>
            <div class="skeleton" style="width:85%;height:12px;margin-bottom:10px"></div>
            <div class="skeleton" style="width:40%;height:11px"></div>
          </div>
        </template>
        <router-link v-else v-for="item in mcpPreview" :key="item.id"
          to="/mcp" class="app-card agent-card"
          :style="{ '--type-color': McpTypeColor[item.type] ?? '#6a7282' }">
          <div class="agent-header">
            <span class="agent-name">{{ item.name }}</span>
            <span class="agent-type-badge"
              :style="{ background: (McpTypeColor[item.type] ?? '#6a7282') + '18', color: McpTypeColor[item.type] ?? '#6a7282' }">
              {{ item.type === 1 ? 'MCP' : 'Agent' }}
            </span>
          </div>
          <p class="agent-desc">{{ item.description }}</p>
          <div class="card-meta">
            <span>{{ item.category }}</span>
            <span>{{ item.useCount }} 使用</span>
          </div>
        </router-link>
      </div>
    </section>

    <!-- 邀请码热门 -->
    <section class="preview-section page-container">
      <div class="section-header">
        <h2 class="section-title">邀请码接龙</h2>
        <router-link to="/codes" class="section-more">查看全部 →</router-link>
      </div>
      <div class="codes-grid">
        <template v-if="codesLoading">
          <div v-for="i in 4" :key="i" class="app-card code-product-card skeleton-card">
            <div class="skeleton" style="width:40px;height:40px;border-radius:10px;margin-bottom:10px"></div>
            <div class="skeleton" style="width:60%;height:15px;margin-bottom:6px"></div>
            <div class="skeleton" style="width:45%;height:12px;margin-bottom:8px"></div>
            <div class="skeleton" style="width:80%;height:12px"></div>
          </div>
        </template>
        <router-link v-else v-for="p in codeProducts" :key="p.id"
          :to="`/product/${p.id}`" class="app-card code-product-card">
          <div class="cp-logo">{{ p.name[0] }}</div>
          <div class="cp-name">{{ p.name }}</div>
          <span class="cp-count" :class="{ available: p.codeCount > 0 }">
            {{ p.codeCount > 0 ? `${p.codeCount} 码可用` : '暂无邀请码' }}
          </span>
          <p class="cp-desc">{{ p.description }}</p>
        </router-link>
      </div>
    </section>

    <!-- 社区引流 -->
    <section class="community-section page-container">
      <div class="section-header">
        <h2 class="section-title">加入社区</h2>
      </div>
      <div class="community-grid">
        <!-- Twitter -->
        <a href="https://x.com/0x_Finderfund" target="_blank" rel="noopener noreferrer" class="community-card twitter-card app-card">
          <div class="community-icon">
            <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
              <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-4.714-6.231-5.401 6.231H2.744l7.73-8.835L1.254 2.25H8.08l4.253 5.622zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
            </svg>
          </div>
          <div class="community-info">
            <div class="community-name">@0x_Finderfund</div>
            <div class="community-desc">关注 Twitter / X，获取最新 AI 动态与邀请码资讯</div>
          </div>
          <span class="community-arrow">→</span>
        </a>

        <!-- 微信群 -->
        <div class="community-card wechat-card app-card">
          <div class="community-icon wechat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.295.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.601-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.49.49 0 0 1 .177-.554 6.37 6.37 0 0 0 2.497-4.609c.044-3.549-3.constant-6.134-6.057-6.134h.001zm-1.48 2.28c.535 0 .969.44.969.983a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.543.434-.983.969-.983zm4.371 0c.535 0 .969.44.969.983a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.543.434-.983.969-.983z"/>
            </svg>
          </div>
          <div class="community-info">
            <div class="community-name">微信群：龙虾4号实验室</div>
            <div class="community-desc">扫码加入 AI 爱好者交流群，分享邀请码与使用心得</div>
          </div>
          <div class="qr-wrapper" @click="showQr = true">
            <img :src="qrSrc" alt="微信群二维码" class="qr-thumb" @error="(e:any) => e.target.style.display='none'" />
            <span class="qr-placeholder">点击查看二维码</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 二维码大图弹窗 -->
    <el-dialog v-model="showQr" title="扫码加入微信群" width="320px" center>
      <div style="text-align:center">
        <img :src="qrSrc" alt="微信群二维码" style="width:260px;border-radius:8px" @error="(e:any) => e.target.style.display='none'" />
        <p style="font-size:13px;color:var(--color-text-muted);margin-top:12px">群聊：🦞龙虾4号实验室</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getResourceList } from '@/api/resource'
import { getSkillList } from '@/api/skill'
import { getMcpList } from '@/api/mcp'
import { getProducts } from '@/api/product'
import { ResourceCategoryColor, SkillCategoryColor, McpTypeColor } from '@/types'
import type { ResourceItem, SkillItem, McpSkillItem } from '@/types'

const resourcePreview = ref<ResourceItem[]>([])
const resourceLoading = ref(false)
const skillsPreview = ref<SkillItem[]>([])
const skillsLoading = ref(false)
const mcpPreview = ref<McpSkillItem[]>([])
const mcpLoading = ref(false)
const codeProducts = ref<any[]>([])
const codesLoading = ref(false)
const showQr = ref(false)
const qrSrc = '/wechat-qr.png'

async function loadResourcePreview() {
  resourceLoading.value = true
  try {
    const res = await getResourceList({ page: 1, size: 3 })
    resourcePreview.value = res.records
  } finally { resourceLoading.value = false }
}

async function loadSkillsPreview() {
  skillsLoading.value = true
  try {
    const res = await getSkillList({ page: 1, size: 3 })
    skillsPreview.value = res.records
  } finally { skillsLoading.value = false }
}

async function loadMcpPreview() {
  mcpLoading.value = true
  try {
    const res = await getMcpList({ page: 1, size: 4 })
    mcpPreview.value = res.records
  } finally { mcpLoading.value = false }
}

async function loadCodeProducts() {
  codesLoading.value = true
  try {
    const all = await getProducts({})
    // 有码的优先，取前4个
    codeProducts.value = [...all]
      .sort((a, b) => (b.codeCount > 0 ? 1 : 0) - (a.codeCount > 0 ? 1 : 0))
      .slice(0, 4)
  } finally { codesLoading.value = false }
}

onMounted(() => {
  loadResourcePreview()
  loadSkillsPreview()
  loadMcpPreview()
  loadCodeProducts()
})
</script>

<style scoped>
.hero {
  padding: 48px 0 36px;
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
}

/* 通用区块 */
.preview-section {
  padding-bottom: 40px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}
.section-more {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
  opacity: 0.8;
  transition: opacity 0.2s;
}
.section-more:hover { opacity: 1; }
.skeleton-card { cursor: default; }
.badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  display: inline-block;
  margin-bottom: 8px;
}
.card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.5;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--color-text-disabled);
}

/* 知识库 */
.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.resource-card {
  padding: 16px;
  text-decoration: none;
  display: block;
  cursor: pointer;
  border-left: 2px solid var(--cat-color, var(--color-border));
  transition: border-color 0.2s;
}
.resource-card:hover { border-left-color: var(--cat-color, var(--color-primary)); }

/* 提示词 */
.skill-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.skill-card {
  padding: 16px;
  text-decoration: none;
  display: block;
  cursor: pointer;
  border-top: 2px solid var(--cat-color, var(--color-border));
  transition: border-color 0.2s;
}
.skill-card:hover { border-top-color: var(--cat-color, var(--color-primary)); }
.skill-prompt {
  font-size: 12px;
  color: var(--color-text-muted);
  font-family: monospace;
  line-height: 1.5;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Agent 技能 */
.agent-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.agent-card {
  padding: 16px;
  text-decoration: none;
  display: block;
  cursor: pointer;
  border-bottom: 2px solid var(--type-color, var(--color-border));
  transition: border-color 0.2s;
}
.agent-card:hover { border-bottom-color: var(--type-color, var(--color-primary)); }
.agent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 8px;
}
.agent-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.agent-type-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.agent-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 邀请码 */
.codes-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.code-product-card {
  padding: 16px;
  text-decoration: none;
  display: block;
  cursor: pointer;
}
.cp-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--color-primary-dim);
  color: var(--color-primary);
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}
.cp-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.cp-count {
  font-size: 11px;
  color: var(--color-text-disabled);
  display: block;
  margin-bottom: 8px;
}
.cp-count.available { color: var(--color-success); }
.cp-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 社区引流 */
.community-section {
  padding-bottom: 60px;
}
.community-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.community-card {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  text-decoration: none;
  cursor: pointer;
}
.community-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--color-bg-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.twitter-card .community-icon { color: #1d9bf0; background: rgba(29,155,240,0.1); }
.twitter-card:hover { border-color: rgba(29,155,240,0.4); }
.wechat-card .community-icon { color: #07c160; background: rgba(7,193,96,0.1); }
.wechat-card:hover { border-color: rgba(7,193,96,0.3); }
.community-info { flex: 1; }
.community-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.community-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}
.community-arrow {
  font-size: 18px;
  color: var(--color-text-disabled);
  flex-shrink: 0;
}
.qr-wrapper {
  cursor: pointer;
  flex-shrink: 0;
}
.qr-thumb {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  object-fit: cover;
  display: block;
}
.qr-placeholder {
  font-size: 11px;
  color: var(--color-primary);
  width: 64px;
  display: block;
  text-align: center;
  line-height: 64px;
}

@media (max-width: 1024px) {
  .agent-grid { grid-template-columns: repeat(2, 1fr); }
  .codes-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .resource-grid, .skill-grid { grid-template-columns: 1fr; }
  .agent-grid, .codes-grid { grid-template-columns: repeat(2, 1fr); }
  .community-grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 24px; }
}
</style>
