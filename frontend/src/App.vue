<template>
  <AppHeader />
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAuthStore } from '@/stores/auth'
import { getMe } from '@/api/user'

const authStore = useAuthStore()

onMounted(async () => {
  if (authStore.token && !authStore.userInfo) {
    try {
      const user = await getMe()
      authStore.setUserInfo(user)
    } catch {
      authStore.logout()
    }
  }
})
</script>
