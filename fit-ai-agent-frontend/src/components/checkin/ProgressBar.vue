<script setup lang="ts">
import type { PlanProgressVO } from '@/types/plan'

defineProps<{
  progress: PlanProgressVO
}>()
</script>

<template>
  <div class="card p-5">
    <div class="flex items-center justify-between mb-3">
      <h2 class="font-heading text-lg font-semibold text-text">
        <template v-if="progress.hasPlan">
          第 {{ progress.currentDay }} 天 / {{ progress.totalDays }} 天
        </template>
        <template v-else>尚未开始计划</template>
      </h2>
      <span
        v-if="progress.hasPlan"
        class="text-sm font-medium text-primary bg-primary/10 px-3 py-1 rounded-full"
      >
        {{ progress.progressPercent }}%
      </span>
    </div>
    <div class="h-2.5 rounded-full bg-background-alt overflow-hidden">
      <div
        class="h-full rounded-full bg-gradient-to-r from-primary to-teal transition-all duration-500 ease-out"
        :style="{ width: `${progress.hasPlan ? progress.progressPercent : 0}%` }"
      />
    </div>
  </div>
</template>
