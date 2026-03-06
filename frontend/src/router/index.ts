import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Home.vue'),
    },
    {
      path: '/codes',
      component: () => import('@/views/Codes.vue'),
    },
    {
      path: '/product/:id',
      component: () => import('@/views/ProductDetail.vue'),
    },
    {
      path: '/news',
      component: () => import('@/views/News.vue'),
    },
    {
      path: '/news/:id',
      component: () => import('@/views/NewsDetail.vue'),
    },
    {
      path: '/skills',
      component: () => import('@/views/Skills.vue'),
    },
    {
      path: '/skills/:id',
      component: () => import('@/views/SkillDetail.vue'),
    },
    {
      path: '/library',
      component: () => import('@/views/Library.vue'),
    },
    {
      path: '/mcp',
      component: () => import('@/views/Mcp.vue'),
    },
    {
      path: '/sites',
      component: () => import('@/views/Sites.vue'),
    },
    {
      path: '/finderFundadmin',
      component: () => import('@/views/Admin.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/register',
      component: () => import('@/views/Register.vue'),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
