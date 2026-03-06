<template>
  <header class="app-header">
    <div class="header-inner page-container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <span class="logo-icon">⬡</span>
        <span class="logo-text">AI 知识库</span>
      </router-link>

      <!-- 导航 -->
      <nav class="header-nav">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/codes" class="nav-item">邀请码</router-link>
        <router-link to="/library" class="nav-item">知识库</router-link>
        <router-link to="/skills" class="nav-item">提示词</router-link>
        <router-link to="/mcp" class="nav-item">Agent技能</router-link>
        <router-link to="/sites" class="nav-item">AI导航</router-link>
      </nav>

      <!-- 右侧操作区 -->
      <div class="header-actions">
        <template v-if="authStore.isLoggedIn">
          <!-- 贡献/投稿 快速入口 -->
          <el-dropdown trigger="click" @command="handleContribute">
            <el-button size="small" type="primary" class="btn-contribute">
              + 贡献
              <el-icon style="margin-left:3px"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu class="contribute-menu">
                <el-dropdown-item command="code">
                  <div class="dropdown-contribute-item">
                    <span>贡献邀请码</span>
                    <span class="item-reward">+30 积分</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="library">
                  <div class="dropdown-contribute-item">
                    <span>投稿知识库</span>
                    <span class="item-reward">+10 积分</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="skill">
                  <div class="dropdown-contribute-item">
                    <span>分享提示词</span>
                    <span class="item-reward">+15 积分</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="site">
                  <div class="dropdown-contribute-item">
                    <span>推荐网站</span>
                    <span class="item-reward">免费</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <!-- 积分徽章（点击去积分任务中心） -->
          <el-tooltip content="查看积分任务" placement="bottom" :show-after="400">
            <router-link to="/profile" class="points-badge">
              <el-icon><Coin /></el-icon>
              <span class="points-num">{{ authStore.userInfo?.points ?? 0 }}</span>
            </router-link>
          </el-tooltip>
          <!-- 用户头像 -->
          <router-link to="/profile">
            <el-avatar
              :size="32"
              class="user-avatar"
            >
              {{ authStore.userInfo?.nickname?.[0]?.toUpperCase() ?? 'U' }}
            </el-avatar>
          </router-link>
        </template>
        <template v-else>
          <router-link to="/login">
            <el-button size="small" class="btn-outline-header">登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button size="small" type="primary">注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

function handleContribute(command: string) {
  if (command === 'code') router.push('/codes')
  if (command === 'library') router.push('/library')
  if (command === 'skill') router.push('/skills')
  if (command === 'site') router.push('/sites')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(9, 9, 15, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
  height: 60px;
}
.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}
.logo-icon {
  font-size: 20px;
  color: var(--color-primary);
  line-height: 1;
}
.logo-text {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.05em;
}
.header-nav {
  flex: 1;
  display: flex;
  gap: 4px;
}
.nav-item {
  padding: 5px 14px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-muted);
  transition: all 0.2s;
  text-decoration: none;
}
.nav-item:hover,
.nav-item.router-link-active {
  background: var(--color-primary-dim);
  color: var(--color-primary);
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.btn-contribute {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-weight: 600;
}
.dropdown-contribute-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-width: 140px;
}
.item-reward {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  background: rgba(0, 210, 239, 0.1);
  padding: 1px 6px;
  border-radius: 4px;
}
.points-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(0, 210, 239, 0.12);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 4px 12px;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font-display);
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s;
}
.points-badge:hover {
  background: rgba(0, 210, 239, 0.2);
}
.points-num {
  min-width: 20px;
  text-align: right;
}
.user-avatar {
  cursor: pointer;
  background: var(--color-primary-dim) !important;
  color: var(--color-primary) !important;
  font-family: var(--font-display);
  font-weight: 700;
  transition: box-shadow 0.2s;
}
.user-avatar:hover {
  box-shadow: 0 0 0 2px var(--color-primary);
}
.btn-outline-header {
  border-color: var(--color-border-hover) !important;
  color: var(--color-primary) !important;
  background: transparent !important;
}
.btn-outline-header:hover {
  background: var(--color-primary-dim) !important;
}
</style>
