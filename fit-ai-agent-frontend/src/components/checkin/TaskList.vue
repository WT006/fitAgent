<script setup lang="ts">
import TaskItem from './TaskItem.vue'
import type { DailyTaskVO } from '@/types/plan'
import { ClipboardList } from 'lucide-vue-next'

defineProps<{
  tasks: DailyTaskVO[]
  canToggle: boolean
  dayIndex: number
  togglingId?: string | null
}>()

const emit = defineEmits<{
  toggle: [taskId: string]
}>()
</script>

<template>
  <div class="card p-5">
    <div class="flex items-center gap-2 mb-4">
      <ClipboardList class="h-5 w-5 text-primary" />
      <h3 class="font-heading text-base font-semibold text-text">
        第 {{ dayIndex }} 天任务
      </h3>
    </div>

    <div v-if="tasks.length === 0" class="py-8 text-center text-sm text-text-muted">
      暂无任务
    </div>

    <div v-else class="space-y-2">
      <TaskItem
        v-for="task in tasks"
        :key="task.id"
        :task="task"
        :can-toggle="canToggle"
        :toggling="togglingId === task.id"
        @toggle="emit('toggle', $event)"
      />
    </div>
  </div>
</template>
