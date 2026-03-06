<template>
  <div class="auth-page">
    <div class="auth-card app-card">
      <div class="auth-logo">
        <span style="color: var(--color-primary); font-size: 28px">⬡</span>
        <h2 class="auth-title">注册 AI 知识库</h2>
      </div>
      <el-form :model="form" label-position="top" size="large">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="图形验证码">
          <div style="display:flex;gap:10px;align-items:center">
            <el-input v-model="form.captchaCode" placeholder="请输入验证码" style="flex:1" />
            <img
              v-if="captcha.imageBase64"
              :src="captcha.imageBase64"
              class="captcha-img"
              title="点击刷新"
              @click="loadCaptcha"
            />
            <div v-else class="captcha-img skeleton"></div>
          </div>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="至少8位" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" style="width:100%;margin-top:8px" size="large"
          :loading="loading" @click="handleRegister">
          注册（赠送 100 积分）
        </el-button>
      </el-form>
      <p class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, register } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const captcha = ref({ captchaId: '', imageBase64: '' })
const form = ref({
  email: '', captchaCode: '', password: '', confirmPassword: ''
})

async function loadCaptcha() {
  try {
    captcha.value = await getCaptcha()
  } catch {}
}

async function handleRegister() {
  const { email, captchaCode, password, confirmPassword } = form.value
  if (!email || !captchaCode || !password || !confirmPassword) {
    ElMessage.warning('请填写完整信息'); return
  }
  if (password !== confirmPassword) {
    ElMessage.error('两次输入的密码不一致'); return
  }
  if (password.length < 8) {
    ElMessage.error('密码至少8位'); return
  }
  loading.value = true
  try {
    await register({
      captchaId: captcha.value.captchaId,
      captchaCode,
      email,
      password,
      inviteCode: route.query.invite as string | undefined,
    })
    ElMessage.success('注册成功，100 积分已到账！')
    router.push('/login')
  } catch {
    loadCaptcha()
    form.value.captchaCode = ''
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 36px 32px;
}
.auth-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
}
.auth-title {
  font-family: var(--font-display);
  font-size: 18px;
  color: var(--color-text-primary);
}
.captcha-img {
  width: 110px;
  height: 40px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  flex-shrink: 0;
}
.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: var(--color-text-muted);
}
</style>
