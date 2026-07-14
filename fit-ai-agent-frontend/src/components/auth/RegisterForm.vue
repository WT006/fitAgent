<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserPlus } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const auth = useAuthStore()
const { success, error: showError } = useToast()

const form = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userName: '',
})
const loading = ref(false)

function validate(): string | null {
  const account = form.value.userAccount.trim()
  if (!account || account.length < 4 || account.length > 16) {
    return '账号长度为 4-16 位'
  }
  if (form.value.userPassword.length < 8) {
    return '密码至少 8 位'
  }
  if (form.value.userPassword !== form.value.checkPassword) {
    return '两次密码不一致'
  }
  return null
}

async function handleSubmit() {
  const err = validate()
  if (err) {
    showError(err)
    return
  }
  loading.value = true
  try {
    await auth.register({
      userAccount: form.value.userAccount,
      userPassword: form.value.userPassword,
      checkPassword: form.value.checkPassword,
      userName: form.value.userName || undefined,
    })
    success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    showError(e instanceof Error ? e.message : '注册失败')
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
        placeholder="长度要求4-16 位"
        autocomplete="username"
      />
    </div>
    <div>
      <label for="userName" class="mb-1.5 block text-sm font-medium text-text">昵称（选填）</label>
      <input
        id="userName"
        v-model="form.userName"
        type="text"
        class="input-field"
        placeholder="显示名称"
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
        autocomplete="new-password"
      />
    </div>
    <div>
      <label for="checkPassword" class="mb-1.5 block text-sm font-medium text-text">确认密码</label>
      <input
        id="checkPassword"
        v-model="form.checkPassword"
        type="password"
        class="input-field"
        placeholder="再次输入密码"
        autocomplete="new-password"
      />
    </div>
    <button type="submit" class="btn-primary w-full" :disabled="loading">
      <UserPlus v-if="!loading" class="h-4 w-4" />
      {{ loading ? '注册中…' : '注册' }}
    </button>
    <p class="text-center text-sm text-text-muted">
      已有账号？
      <RouterLink to="/login" class="font-medium text-primary hover:underline cursor-pointer">
        去登录
      </RouterLink>
    </p>
  </form>
</template>
