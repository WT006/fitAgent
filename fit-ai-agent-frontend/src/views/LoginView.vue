<script setup lang="ts">
import { Leaf } from 'lucide-vue-next'
import LoginForm from '@/components/auth/LoginForm.vue'
</script>

<template>
  <div class="min-h-dvh flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-teal shadow-md">
          <Leaf class="h-7 w-7 text-white" />
        </div>
        <h1 class="font-heading text-2xl font-bold text-text">欢迎回来</h1>
        <p class="mt-2 text-sm text-text-muted">登录 fitagent，继续你的健康之旅</p>
      </div>
      <div class="card p-6">
        <LoginForm />
      </div>
    </div>
  </div>
</template>
