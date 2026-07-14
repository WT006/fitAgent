<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next'

defineProps<{
  message?: string
}>()
</script>

<template>
  <div class="fixed inset-0 z-[300] flex items-center justify-center bg-white/80 backdrop-blur-sm">
    <div class="flex flex-col items-center gap-4 text-center px-6">
      <Loader2 class="h-10 w-10 animate-spin text-primary" />
      <p class="font-heading text-lg font-medium text-text">
        {{ message || 'AI 正在生成你的 30 天计划…' }}
      </p>
      <p class="text-sm text-text-muted">这可能需要 10-30 秒，请耐心等待</p>
    </div>
  </div>
</template>
