import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/checkin' },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/checkin',
      name: 'checkin',
      component: () => import('@/views/CheckinView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/ChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/chat/consult',
      name: 'chat-consult',
      component: () => import('@/views/ChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/chat/agent',
      name: 'chat-agent',
      component: () => import('@/views/ChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/AdminUsersView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (auth.token && !auth.user) {
    try {
      await auth.fetchCurrentUser()
    } catch {
      auth.logout()
    }
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { path: '/checkin' }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { path: '/checkin' }
  }
})

export default router
