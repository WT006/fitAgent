<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Activity, MessageCircle, LogOut, Leaf, Users } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { success } = useToast()

const navItems = computed(() => {
  const items = [
    { path: '/checkin', label: '打卡', icon: Activity },
    { path: '/chat', label: 'AI 对话', icon: MessageCircle },
  ]
  if (auth.isAdmin) {
    items.push({ path: '/admin/users', label: '用户管理', icon: Users })
  }
  return items
})

const isActive = (path: string) => {
  if (path === '/chat') {
    return route.path.startsWith('/chat') && route.query.mode !== 'planning'
  }
  return route.path === path || route.path.startsWith(path + '/')
}

async function handleLogout() {
  await auth.logout()
  success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <header class="sticky top-0 z-50 border-b border-border/60 bg-white/80 backdrop-blur-md">
    <div
      class="mx-auto flex h-14 items-center justify-between px-4"
      :class="route.path.startsWith('/admin') ? 'max-w-5xl' : 'max-w-2xl'"
    >
      <RouterLink to="/checkin" class="flex items-center gap-2 cursor-pointer">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-teal">
          <Leaf class="h-4 w-4 text-white" />
        </div>
        <span class="font-heading text-lg font-semibold text-text">fitagent</span>
      </RouterLink>

      <nav class="flex items-center gap-1">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors duration-200 cursor-pointer"
          :class="
            isActive(item.path)
              ? 'bg-primary/10 text-primary'
              : 'text-text-muted hover:bg-background-alt hover:text-text'
          "
        >
          <component :is="item.icon" class="h-4 w-4" />
          <span class="hidden sm:inline">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="flex items-center gap-2">
        <span class="hidden sm:inline text-sm text-text-secondary truncate max-w-[100px]">
          {{ auth.user?.userName || auth.user?.userAccount }}
        </span>
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center rounded-lg text-text-muted hover:bg-red-50 hover:text-red-600 transition-colors cursor-pointer"
          aria-label="退出登录"
          @click="handleLogout"
        >
          <LogOut class="h-4 w-4" />
        </button>
      </div>
    </div>
  </header>
</template>
