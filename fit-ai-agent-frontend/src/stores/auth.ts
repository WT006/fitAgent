import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as userApi from '@/api/user'
import { getToken, removeToken, setToken } from '@/api/request'
import type { LoginForm, LoginUserVO, RegisterForm } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<LoginUserVO | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.userRole === 'admin')

  async function login(form: LoginForm) {
    const result = await userApi.login(form)
    token.value = result.token
    user.value = result.userVO
    setToken(result.token)
  }

  async function register(form: RegisterForm) {
    await userApi.register(form)
  }

  async function logout() {
    try {
      await userApi.logout()
    } finally {
      token.value = null
      user.value = null
      removeToken()
    }
  }

  async function fetchCurrentUser() {
    const data = await userApi.getCurrentUser()
    user.value = data
    return data
  }

  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    login,
    register,
    logout,
    fetchCurrentUser,
  }
})
