<script setup lang="ts">
import { Sparkles, Leaf } from 'lucide-vue-next'

const emit = defineEmits<{
  plan: []
}>()
</script>

<template>
  <div class="card p-8 text-center">
    <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary/20 to-teal/20">
      <Leaf class="h-8 w-8 text-primary" />
    </div>
    <h3 class="font-heading text-xl font-semibold text-text mb-2">开始你的 30 天健康之旅</h3>
    <p class="text-sm text-text-muted mb-6 leading-relaxed">
      还没有健康计划？让 AI 根据你的情况，为你定制专属的 30 天打卡任务。
    </p>
    <button type="button" class="btn-primary" @click="emit('plan')">
      <Sparkles class="h-4 w-4" />
      AI 帮我规划
    </button>
  </div>
</template>
