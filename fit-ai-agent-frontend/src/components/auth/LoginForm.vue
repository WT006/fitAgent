<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LogIn } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { error: showError } = useToast()

const form = ref({
  userAccount: '',
  userPassword: '',
})
const loading = ref(false)

async function handleSubmit() {
  if (!form.value.userAccount || !form.value.userPassword) {
    showError('请填写账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.value)
    const redirect = (route.query.redirect as string) || '/checkin'
    router.push(redirect)
  } catch (e) {
    showError(e instanceof Error ? e.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="space-y-5" @submit.prevent="handleSubmit">
    <div>
      <label for="userAccount" class="mb-1.5 block text-sm font-medium text-text">账号</label>
      <input
        id="userAccount"
        v-model="form.userAccount"
        type="text"
        class="input-field"
        placeholder="4-16 位字母/数字/下划线"
        autocomplete="username"
      />
    </div>
    <div>
      <label for="userPassword" class="mb-1.5 block text-sm font-medium text-text">密码</label>
      <input
        id="userPassword"
        v-model="form.userPassword"
        type="password"
        class="input-field"
        placeholder="至少 8 位"
        autocomplete="current-password"
      />
    </div>
    <button type="submit" class="btn-primary w-full" :disabled="loading">
      <LogIn v-if="!loading" class="h-4 w-4" />
      {{ loading ? '登录中…' : '登录' }}
    </button>
    <p class="text-center text-sm text-text-muted">
      还没有账号？
      <RouterLink to="/register" class="font-medium text-primary hover:underline cursor-pointer">
        立即注册
      </RouterLink>
    </p>
  </form>
</template>
