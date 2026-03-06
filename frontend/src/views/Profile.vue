<template>
  <div class="page-container profile-page">
    <!-- 用户信息头 -->
    <div class="profile-header app-card">
      <el-avatar :size="60" class="user-avatar-lg">
        {{ authStore.userInfo?.nickname?.[0]?.toUpperCase() ?? 'U' }}
      </el-avatar>
      <div class="user-info">
        <div class="user-name">{{ authStore.userInfo?.nickname }}</div>
        <div class="user-email">{{ authStore.userInfo?.email }}</div>
        <div class="points-row">
          <span class="points-badge-lg">
            <el-icon><Coin /></el-icon>
            {{ authStore.userInfo?.points ?? 0 }} 积分
          </span>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :loading="checkingIn" @click="handleCheckin">
          每日签到 +10
        </el-button>
        <el-button @click="authStore.logout()">退出</el-button>
      </div>
    </div>

    <!-- Tab 区域 -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 积分流水 -->
      <el-tab-pane label="积分流水" name="points">
        <!-- 任务中心快捷入口 -->
        <div class="task-shortcut" @click="activeTab = 'tasks'">
          <span class="task-shortcut-text">完成任务可赚取更多积分</span>
          <span class="task-shortcut-link">查看任务中心 →</span>
        </div>
        <div v-if="loadingPoints" class="tab-loading">加载中...</div>
        <div v-else-if="pointRecords.length" class="record-list">
          <div v-for="r in pointRecords" :key="r.id" class="app-card record-item">
            <div class="record-left">
              <span class="record-remark">{{ r.remark }}</span>
              <span class="record-time">{{ formatRelativeTime(r.createTime) }}</span>
            </div>
            <span class="record-delta" :class="r.delta > 0 ? 'positive' : 'negative'">
              {{ r.delta > 0 ? `+${r.delta}` : r.delta }}
            </span>
          </div>
        </div>
        <div v-else class="empty-state">暂无积分记录</div>
      </el-tab-pane>

      <!-- 我的邀请码 -->
      <el-tab-pane label="我的邀请码" name="codes">
        <div v-if="loadingMyCodes" class="tab-loading">加载中...</div>
        <div v-else-if="myCodes.length" class="record-list">
          <div v-for="c in myCodes" :key="c.id" class="app-card record-item">
            <div class="record-left">
              <span class="code-preview-sm">{{ c.codePreview }}</span>
              <span class="record-time">{{ formatRelativeTime(c.createTime) }}</span>
            </div>
            <span class="code-status" :class="codeContribStatusClass(c.status)">
              {{ codeContribStatusText(c.status) }}
            </span>
          </div>
        </div>
        <div v-else class="empty-state">还没有贡献过邀请码</div>
      </el-tab-pane>

      <!-- 任务中心 -->
      <el-tab-pane name="tasks">
        <template #label>
          <span class="task-tab-label">
            任务中心
            <span v-if="pendingTaskCount > 0" class="task-tab-badge">{{ pendingTaskCount }}</span>
          </span>
        </template>
        <!-- 可赚积分汇总 -->
        <div v-if="!loadingTasks && pendingTaskPoints > 0" class="task-summary">
          还有 <b>{{ pendingTaskCount }}</b> 个任务未完成，可再赚 <b class="points-earn">+{{ pendingTaskPoints }} 积分</b>
        </div>
        <div v-if="loadingTasks" class="tab-loading">加载中...</div>
        <div v-else-if="tasks.length" class="record-list">
          <div v-for="t in tasks" :key="t.id" class="app-card record-item" :class="{ 'task-pending': !t.completed }">
            <div class="record-left">
              <span class="record-remark">{{ t.name }}</span>
              <span class="freq-tag">{{ freqLabel(t.frequency) }}</span>
            </div>
            <div style="display:flex;align-items:center;gap:10px">
              <span :style="{ color: t.completed ? 'var(--color-text-disabled)' : 'var(--color-primary)', fontSize: '13px', fontWeight: t.completed ? '400' : '600' }">+{{ t.pointsReward }} 积分</span>
              <el-tag :type="t.completed ? 'success' : 'warning'" size="small">
                {{ t.completed ? '已完成' : '去完成' }}
              </el-tag>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无任务</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { checkin, getPointRecords } from '@/api/point'
import { getMyContributed } from '@/api/code'
import { getTasks } from '@/api/task'
import { getMe } from '@/api/user'
import { formatRelativeTime } from '@/utils/format'

const authStore = useAuthStore()

const activeTab = ref('points')
const checkingIn = ref(false)

const pointRecords = ref<any[]>([])
const loadingPoints = ref(false)
const myCodes = ref<any[]>([])
const loadingMyCodes = ref(false)
const tasks = ref<any[]>([])
const loadingTasks = ref(false)
const pendingTaskCount = computed(() => tasks.value.filter(t => !t.completed).length)
const pendingTaskPoints = computed(() => tasks.value.filter(t => !t.completed).reduce((s, t) => s + (t.pointsReward ?? 0), 0))

async function handleCheckin() {
  checkingIn.value = true
  try {
    await checkin()
    ElMessage.success('签到成功，+10 积分！')
    const user = await getMe()
    authStore.setUserInfo(user)
    // 刷新积分流水
    if (activeTab.value === 'points') loadPointRecords()
  } catch {
    // 已签到的错误由 request 拦截器统一提示
  } finally {
    checkingIn.value = false
  }
}

async function loadPointRecords() {
  loadingPoints.value = true
  try { pointRecords.value = (await getPointRecords()).records } finally { loadingPoints.value = false }
}

async function loadMyCodes() {
  loadingMyCodes.value = true
  try { myCodes.value = (await getMyContributed()).records } finally { loadingMyCodes.value = false }
}

async function loadTasks() {
  loadingTasks.value = true
  try { tasks.value = await getTasks() } finally { loadingTasks.value = false }
}

function freqLabel(freq: number) {
  return freq === 1 ? '每日' : freq === 2 ? '每周' : '一次性'
}

function codeContribStatusText(status: number) {
  const map: Record<number, string> = {
    5: '待审核', 1: '池中可用', 6: '已拒绝',
    2: '已被领取', 3: '确认有效 +30', 4: '确认无效'
  }
  return map[status] ?? '—'
}
function codeContribStatusClass(status: number) {
  if (status === 3) return 'available'
  if (status === 6 || status === 4) return 'used'
  return ''
}

watch(activeTab, (tab) => {
  if (tab === 'points' && !pointRecords.value.length) loadPointRecords()
  if (tab === 'codes' && !myCodes.value.length) loadMyCodes()
  if (tab === 'tasks' && !tasks.value.length) loadTasks()
})

onMounted(() => {
  loadPointRecords()
  loadTasks()  // 预加载任务以显示未完成数
})
</script>

<style scoped>
.profile-page {
  padding-top: 28px;
  padding-bottom: 60px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.user-avatar-lg {
  background: var(--color-primary-dim) !important;
  color: var(--color-primary) !important;
  font-family: var(--font-display);
  font-size: 24px !important;
  font-weight: 700;
  flex-shrink: 0;
}
.user-info { flex: 1; min-width: 0 }
.user-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.user-email {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 10px;
}
.points-row { display: flex; align-items: center; gap: 8px }
.points-badge-lg {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(0, 210, 239, 0.12);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 4px 14px;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-display);
}
.header-actions { display: flex; gap: 8px; flex-shrink: 0 }
.profile-tabs { margin-top: 8px }
.tab-loading {
  text-align: center;
  padding: 40px;
  color: var(--color-text-muted);
  font-size: 14px;
}
.record-list { display: flex; flex-direction: column; gap: 8px }
.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  gap: 10px;
}
.record-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.record-remark {
  font-size: 14px;
  color: var(--color-text-primary);
  white-space: nowrap;
}
.record-time { font-size: 12px; color: var(--color-text-disabled) }
.record-delta {
  font-size: 15px;
  font-weight: 600;
  font-family: var(--font-display);
  flex-shrink: 0;
}
.record-delta.positive { color: var(--color-success) }
.record-delta.negative { color: var(--color-error) }
.code-preview-sm {
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--color-primary);
  letter-spacing: 0.05em;
}
.chain-tag {
  font-size: 11px;
  color: var(--color-text-disabled);
  background: var(--color-bg-overlay);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
}
.code-status { font-size: 13px; font-weight: 500; flex-shrink: 0 }
.code-status.available { color: var(--color-text-muted) }
.code-status.used { color: var(--color-success) }
.freq-tag {
  font-size: 11px;
  color: var(--color-text-disabled);
  background: var(--color-bg-overlay);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
}
.empty-state {
  text-align: center;
  padding: 48px 0;
  color: var(--color-text-muted);
  font-size: 14px;
}
/* 任务中心快捷入口 */
.task-shortcut {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(0, 210, 239, 0.06);
  border: 1px solid rgba(0, 210, 239, 0.15);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  margin-bottom: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.task-shortcut:hover { background: rgba(0, 210, 239, 0.1); }
.task-shortcut-text {
  font-size: 13px;
  color: var(--color-text-muted);
}
.task-shortcut-link {
  font-size: 13px;
  color: var(--color-primary);
  font-weight: 500;
  white-space: nowrap;
}
/* 任务 Tab 标签徽章 */
.task-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.task-tab-badge {
  font-size: 11px;
  font-weight: 700;
  background: var(--color-primary);
  color: #09090f;
  border-radius: 10px;
  padding: 0 5px;
  min-width: 16px;
  text-align: center;
  line-height: 16px;
}
/* 任务汇总横幅 */
.task-summary {
  font-size: 13px;
  color: var(--color-text-muted);
  background: rgba(0, 210, 239, 0.06);
  border: 1px solid rgba(0, 210, 239, 0.15);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  margin-bottom: 14px;
  line-height: 1.6;
}
.task-summary b { color: var(--color-text-primary); }
.points-earn { color: var(--color-primary) !important; }
/* 未完成任务高亮 */
.task-pending {
  border-left: 2px solid rgba(0, 210, 239, 0.4);
}
@media (max-width: 600px) {
  .profile-header { flex-direction: column; align-items: flex-start }
  .header-actions { width: 100% }
}
</style>
