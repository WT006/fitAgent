<script setup lang="ts">
import { getTaskTypeMeta } from '@/utils/taskType'
import type { PreviewDay } from '@/types/plan'

defineProps<{
  day: PreviewDay
}>()

const emit = defineEmits<{
  'update:title': [taskIndex: number, title: string]
}>()
</script>

<template>
  <div class="card p-4">
    <h4 class="font-heading text-sm font-semibold text-primary mb-3">
      第 {{ day.dayIndex }} 天
    </h4>
    <div class="space-y-2">
      <div
        v-for="(task, idx) in day.tasks"
        :key="idx"
        class="flex items-center gap-2"
      >
        <input
          :value="task.title"
          type="text"
          class="input-field flex-1 text-sm py-2"
          @input="emit('update:title', idx, ($event.target as HTMLInputElement).value)"
        />
        <span
          class="shrink-0 rounded-full px-2 py-0.5 text-xs font-medium"
          :class="[getTaskTypeMeta(task.type).bgClass, getTaskTypeMeta(task.type).colorClass]"
        >
          {{ getTaskTypeMeta(task.type).label }}
        </span>
      </div>
    </div>
  </div>
</template>
