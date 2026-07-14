<script setup lang="ts">
import { ref, computed } from 'vue'
import PreviewDayCard from './PreviewDayCard.vue'
import QuestionStep from './QuestionStep.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import type { PlanningPhase, PreviewDay } from '@/types/plan'
import { CheckCircle, ArrowLeft } from 'lucide-vue-next'

const props = defineProps<{
  phase: PlanningPhase
  question: string | null
  questionIndex: number | null
  preview: PreviewDay[] | null
  loading: boolean
  confirming: boolean
}>()

const emit = defineEmits<{
  submit: [answer: string]
  confirm: []
  back: []
}>()

const expandedDay = ref<number | null>(1)

const displayDays = computed(() => {
  if (!props.preview) return []
  if (expandedDay.value === null) return props.preview
  return props.preview.filter((d) => d.dayIndex === expandedDay.value)
})

function updateTitle(dayIndex: number, taskIndex: number, title: string) {
  if (!props.preview) return
  const day = props.preview.find((d) => d.dayIndex === dayIndex)
  if (day) day.tasks[taskIndex].title = title
}
</script>

<template>
  <div>
    <LoadingOverlay v-if="phase === 'generating'" />

    <div v-if="phase === 'question'" class="space-y-4">
      <button
        type="button"
        class="flex items-center gap-1 text-sm text-text-muted hover:text-text transition-colors cursor-pointer"
        @click="emit('back')"
      >
        <ArrowLeft class="h-4 w-4" />
        返回打卡页
      </button>
      <QuestionStep
        :question="question || ''"
        :question-index="questionIndex"
        :loading="loading"
        @submit="emit('submit', $event)"
      />
    </div>

    <div v-else-if="phase === 'preview' && preview" class="space-y-4">
      <div class="flex items-center justify-between">
        <button
          type="button"
          class="flex items-center gap-1 text-sm text-text-muted hover:text-text transition-colors cursor-pointer"
          @click="emit('back')"
        >
          <ArrowLeft class="h-4 w-4" />
          返回
        </button>
        <span class="text-sm text-text-muted">共 30 天计划，可编辑任务标题</span>
      </div>

      <div class="flex gap-2 overflow-x-auto pb-2 scrollbar-thin">
        <button
          v-for="day in preview"
          :key="day.dayIndex"
          type="button"
          class="shrink-0 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors cursor-pointer"
          :class="
            expandedDay === day.dayIndex
              ? 'bg-primary text-white'
              : 'bg-white border border-border text-text-muted hover:border-primary/40'
          "
          @click="expandedDay = day.dayIndex"
        >
          第{{ day.dayIndex }}天
        </button>
      </div>

      <PreviewDayCard
        v-for="day in displayDays"
        :key="day.dayIndex"
        :day="day"
        @update:title="(idx, title) => updateTitle(day.dayIndex, idx, title)"
      />

      <button
        type="button"
        class="btn-primary w-full py-3"
        :disabled="confirming"
        @click="emit('confirm')"
      >
        <CheckCircle class="h-5 w-5" />
        {{ confirming ? '确认中…' : '确认计划' }}
      </button>
    </div>
  </div>
</template>
