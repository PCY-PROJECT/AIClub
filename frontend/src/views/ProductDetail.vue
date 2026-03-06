<template>
  <div class="page-container detail-page">
    <!-- 产品信息头 -->
    <div v-if="product" class="product-header app-card">
      <div class="product-logo-lg">{{ product.name[0] }}</div>
      <div class="product-info">
        <h1 class="product-name">{{ product.name }}</h1>
        <div class="product-meta">
          <span class="category-tag">{{ CategoryMap[product.category] ?? '其他' }}</span>
          <span class="code-avail">池中可用邀请码：{{ product.codeCount }}</span>
        </div>
        <p class="product-desc">{{ product.description }}</p>
      </div>
      <a v-if="product.officialUrl" :href="product.officialUrl" target="_blank" class="official-btn">
        去官网 ↗
      </a>
    </div>
    <div v-else-if="loadingProduct" class="product-header app-card skeleton-card">
      <div class="skeleton" style="width:64px;height:64px;border-radius:14px"></div>
      <div style="flex:1;margin-left:16px">
        <div class="skeleton" style="width:120px;height:22px;margin-bottom:10px"></div>
        <div class="skeleton" style="width:200px;height:14px"></div>
      </div>
    </div>

    <!-- 邀请码池区域 -->
    <div class="codes-section">
      <h2 class="section-title">获取邀请码</h2>

      <!-- 已获取待确认 -->
      <div v-if="claimedCode" class="app-card claimed-card">
        <div class="claimed-header">
          <span class="claimed-badge">已获取</span>
          <span class="deadline-text">
            确认截止：{{ formatDeadline(claimedCode.confirmDeadline) }}
          </span>
        </div>
        <div class="code-display">{{ claimedCode.codeText }}</div>
        <el-button style="width:100%;margin-top:10px" @click="copyClaimedCode">
          {{ copied ? '已复制 ✓' : '复制邀请码' }}
        </el-button>

        <!-- 如果已确认则显示结果，否则显示确认按钮 -->
        <template v-if="claimedCode.status === 2">
          <el-divider />
          <p class="confirm-hint">请使用邀请码注册后，反馈是否有效（12小时内未反馈将自动判定为有效）</p>
          <div class="confirm-actions">
            <el-button type="success" :loading="confirming === 'valid'" @click="doConfirm('valid')">
              ✓ 邀请码有效
            </el-button>
            <el-button type="danger" plain :loading="confirming === 'invalid'" @click="doConfirm('invalid')">
              ✗ 邀请码无效（退还50积分）
            </el-button>
          </div>
        </template>
        <template v-else>
          <el-divider />
          <div class="confirmed-result">
            <el-tag v-if="claimedCode.status === 3" type="success" size="large">已确认有效</el-tag>
            <el-tag v-else-if="claimedCode.status === 4" type="danger" size="large">已确认无效（已退还积分）</el-tag>
          </div>
        </template>
      </div>

      <!-- 未获取 — 显示获取按钮 -->
      <div v-else class="app-card pool-card">
        <div class="pool-info">
          <div class="pool-count">
            <span class="pool-num">{{ product?.codeCount ?? 0 }}</span>
            <span class="pool-label">个可用邀请码</span>
          </div>
          <p class="pool-desc">花费 50 积分获取一个随机邀请码，获取后 12 小时内需确认有效性。</p>
        </div>
        <el-button
          type="primary" size="large" style="width:100%"
          :loading="claiming"
          :disabled="!authStore.isLoggedIn || (product?.codeCount ?? 0) === 0"
          @click="doClaim"
        >
          {{ !authStore.isLoggedIn ? '请先登录' : '花50积分获取邀请码' }}
        </el-button>
        <p v-if="(product?.codeCount ?? 0) === 0" class="pool-empty-hint">暂无可用邀请码</p>
      </div>
    </div>

    <!-- 悬浮贡献按钮 -->
    <div class="fab-area">
      <el-button type="primary" size="large" round @click="openContributeDialog">
        ＋ 贡献邀请码
      </el-button>
      <div class="fab-reward-hint">审核通过可获 +30 积分</div>
    </div>

    <!-- ── 贡献邀请码弹窗 ── -->
    <el-dialog v-model="contributeVisible" title="贡献邀请码" width="420px" align-center>
      <p style="font-size:13px;color:var(--color-text-muted);margin-bottom:14px;line-height:1.7">
        贡献的邀请码需经过管理员审核后才会进入池子。<br>
        当他人获取并确认有效后，你将获得 <b style="color:var(--color-primary)">+30 积分</b>。
      </p>
      <el-input
        v-model="contributeContent"
        :placeholder="`请输入 ${product?.name ?? ''} 的邀请码`"
        size="large"
      />
      <template #footer>
        <el-button @click="contributeVisible = false">取消</el-button>
        <el-button type="primary" :loading="contributing" @click="doContribute">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct } from '@/api/product'
import { claimCode, confirmCode, contributeCode, getMyClaimed } from '@/api/code'
import { useAuthStore } from '@/stores/auth'
import { CategoryMap } from '@/types'

const route = useRoute()
const authStore = useAuthStore()
const productId = Number(route.params.id)

const product = ref<any>(null)
const loadingProduct = ref(false)

// 已获取的邀请码（待确认或已确认）
const claimedCode = ref<any>(null)

// 获取流程
const claiming = ref(false)

// 确认流程
const confirming = ref<'valid' | 'invalid' | null>(null)

// 复制
const copied = ref(false)

// 贡献弹窗
const contributeVisible = ref(false)
const contributing = ref(false)
const contributeContent = ref('')

async function loadProduct() {
  loadingProduct.value = true
  try { product.value = await getProduct(productId) } finally { loadingProduct.value = false }
}

/** 加载当前用户针对该产品的已获取邀请码（status=2 待确认） */
async function loadMyClaimed() {
  if (!authStore.isLoggedIn) return
  try {
    const result = await getMyClaimed(1, 50)
    const records = result?.records ?? []
    // 找该产品的最新一条（优先 status=2，其次最近一条）
    const forProduct = records.filter((r: any) => r.productId === productId)
    const pending = forProduct.find((r: any) => r.status === 2)
    claimedCode.value = pending ?? forProduct[0] ?? null
  } catch {
    claimedCode.value = null
  }
}

async function doClaim() {
  if (!authStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  claiming.value = true
  try {
    const result = await claimCode(productId)
    claimedCode.value = result
    copied.value = false
    // 更新积分显示
    if (authStore.userInfo) {
      authStore.updatePoints(authStore.userInfo.points - 50)
    }
    // 更新产品可用数
    if (product.value && product.value.codeCount > 0) {
      product.value.codeCount--
    }
    ElMessage.success('获取成功！请12小时内确认是否有效')
  } finally {
    claiming.value = false
  }
}

async function doConfirm(result: 'valid' | 'invalid') {
  if (!claimedCode.value) return
  confirming.value = result
  try {
    await confirmCode(claimedCode.value.codeId, result)
    if (result === 'valid') {
      claimedCode.value.status = 3
      claimedCode.value.confirmResult = 1
      ElMessage.success('已确认有效，感谢反馈！提供者已获得积分奖励')
    } else {
      claimedCode.value.status = 4
      claimedCode.value.confirmResult = 2
      ElMessage.success('已确认无效，积分已退还')
      // 退款后更新积分显示
      if (authStore.userInfo) {
        authStore.updatePoints(authStore.userInfo.points + 50)
      }
    }
  } finally {
    confirming.value = null
  }
}

async function copyClaimedCode() {
  if (!claimedCode.value?.codeText) return
  try {
    await navigator.clipboard.writeText(claimedCode.value.codeText)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

function openContributeDialog() {
  if (!authStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  contributeContent.value = ''
  contributeVisible.value = true
}

async function doContribute() {
  if (!contributeContent.value.trim()) { ElMessage.warning('请输入邀请码'); return }
  contributing.value = true
  try {
    await contributeCode({ productId, code: contributeContent.value.trim() })
    ElMessage.success('提交成功！等待管理员审核，审核通过后进入池子')
    contributeVisible.value = false
  } finally {
    contributing.value = false
  }
}

function formatDeadline(deadline: string) {
  if (!deadline) return '—'
  return deadline.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadProduct()
  loadMyClaimed()
})
</script>

<style scoped>
.detail-page {
  padding-top: 28px;
  padding-bottom: 100px;
}
.product-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  margin-bottom: 28px;
}
.product-logo-lg {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  background: var(--color-primary-dim);
  color: var(--color-primary);
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.product-info { flex: 1 }
.product-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}
.product-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.category-tag {
  font-size: 12px;
  color: var(--color-text-muted);
  background: var(--color-bg-overlay);
  padding: 2px 10px;
  border-radius: var(--radius-sm);
}
.code-avail { font-size: 13px; color: var(--color-primary) }
.product-desc {
  font-size: 14px;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin: 0;
}
.official-btn {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--color-primary);
  border: 1px solid var(--color-border-hover);
  border-radius: var(--radius-sm);
  padding: 4px 12px;
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.2s;
}
.official-btn:hover { background: var(--color-primary-dim) }
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 14px;
}

/* 已获取邀请码卡片 */
.claimed-card {
  padding: 20px;
}
.claimed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.claimed-badge {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-dim);
  padding: 2px 10px;
  border-radius: var(--radius-sm);
}
.deadline-text {
  font-size: 12px;
  color: var(--color-text-muted);
}
.code-display {
  font-family: var(--font-mono);
  background: rgba(0, 210, 239, 0.08);
  border: 1px solid rgba(0, 210, 239, 0.25);
  border-radius: var(--radius-md);
  padding: 14px 18px;
  color: var(--color-primary);
  letter-spacing: 0.08em;
  font-size: 16px;
  text-align: center;
  word-break: break-all;
}
.confirm-hint {
  font-size: 13px;
  color: var(--color-text-muted);
  text-align: center;
  margin-bottom: 14px;
  line-height: 1.6;
}
.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.confirm-actions .el-button {
  flex: 1;
}
.confirmed-result {
  text-align: center;
  padding: 4px 0;
}

/* 池子卡片 */
.pool-card {
  padding: 28px 24px;
  text-align: center;
}
.pool-info {
  margin-bottom: 20px;
}
.pool-count {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  margin-bottom: 10px;
}
.pool-num {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 700;
  color: var(--color-primary);
}
.pool-label {
  font-size: 14px;
  color: var(--color-text-muted);
}
.pool-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.7;
  margin: 0;
}
.pool-empty-hint {
  font-size: 12px;
  color: var(--color-text-disabled);
  margin-top: 10px;
}

.fab-area {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.fab-reward-hint {
  font-size: 11px;
  color: var(--color-primary);
  background: rgba(0, 210, 239, 0.1);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  white-space: nowrap;
  font-weight: 500;
}
.skeleton-card { cursor: default }
@media (max-width: 768px) {
  .product-header { flex-wrap: wrap }
  .fab-area { bottom: 20px; right: 16px }
  .confirm-actions { flex-direction: column }
}
</style>
