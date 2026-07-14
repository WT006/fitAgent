<script setup lang="ts">
import { Leaf } from 'lucide-vue-next'
import RegisterForm from '@/components/auth/RegisterForm.vue'
</script>

<template>
  <div class="min-h-dvh flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-teal shadow-md">
          <Leaf class="h-7 w-7 text-white" />
        </div>
        <h1 class="font-heading text-2xl font-bold text-text">创建账号</h1>
        <p class="mt-2 text-sm text-text-muted">加入 fitagent，开启 30 天健康计划</p>
      </div>
      <div class="card p-6">
        <RegisterForm />
      </div>
    </div>
  </div>
</template>
