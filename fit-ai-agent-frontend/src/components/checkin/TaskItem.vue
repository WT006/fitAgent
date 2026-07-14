<script setup lang="ts">
import { getTaskTypeMeta } from '@/utils/taskType'
import type { DailyTaskVO } from '@/types/plan'
import { Check } from 'lucide-vue-next'

defineProps<{
  task: DailyTaskVO
  canToggle: boolean
  toggling?: boolean
}>()

const emit = defineEmits<{
  toggle: [taskId: string]
}>()
</script>

<template>
  <label
    class="flex items-center gap-3 rounded-xl border border-border/60 bg-white/60 px-4 py-3.5 transition-all duration-200"
    :class="{
      'hover:border-primary/40 cursor-pointer': canToggle && !toggling,
      'opacity-60 cursor-not-allowed': !canToggle,
      'border-primary/30 bg-primary/5': task.status === 1,
    }"
  >
    <div class="relative shrink-0">
      <input
        type="checkbox"
        class="peer sr-only"
        :checked="task.status === 1"
        :disabled="!canToggle || toggling"
        @change="emit('toggle', task.id)"
      />
      <div
        class="flex h-6 w-6 items-center justify-center rounded-lg border-2 transition-all duration-200"
        :class="
          task.status === 1
            ? 'border-primary bg-primary text-white'
            : 'border-gray-300 bg-white peer-focus-visible:ring-2 peer-focus-visible:ring-primary/30'
        "
      >
        <Check v-if="task.status === 1" class="h-3.5 w-3.5" />
      </div>
    </div>
    <span
      class="flex-1 text-sm font-medium transition-all duration-200"
      :class="task.status === 1 ? 'text-text-muted line-through' : 'text-text'"
    >
      {{ task.title }}
    </span>
    <span
      class="shrink-0 rounded-full px-2.5 py-0.5 text-xs font-medium"
      :class="[getTaskTypeMeta(task.type).bgClass, getTaskTypeMeta(task.type).colorClass]"
    >
      {{ getTaskTypeMeta(task.type).label }}
    </span>
  </label>
</template>
