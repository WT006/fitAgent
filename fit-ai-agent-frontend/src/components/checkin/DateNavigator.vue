<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { formatDisplayDate, isToday } from '@/utils/date'

const props = defineProps<{
  dates: string[]
  selectedDate: string
}>()

const emit = defineEmits<{
  change: [date: string]
}>()

const currentIndex = computed(() => props.dates.indexOf(props.selectedDate))

function goPrev() {
  if (currentIndex.value > 0) {
    emit('change', props.dates[currentIndex.value - 1])
  }
}

function goNext() {
  if (currentIndex.value < props.dates.length - 1) {
    emit('change', props.dates[currentIndex.value + 1])
  }
}

function selectDate(date: string) {
  emit('change', date)
}

const visibleDates = computed(() => {
  const idx = currentIndex.value
  const start = Math.max(0, idx - 1)
  const end = Math.min(props.dates.length, start + 3)
  return props.dates.slice(start, end)
})
</script>

<template>
  <div class="card p-4">
    <div class="flex items-center justify-between">
      <button
        type="button"
        class="flex h-10 w-10 items-center justify-center rounded-xl text-text-muted hover:bg-background-alt hover:text-text transition-colors disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentIndex <= 0"
        aria-label="上一天"
        @click="goPrev"
      >
        <ChevronLeft class="h-5 w-5" />
      </button>

      <div class="flex items-center gap-2">
        <button
          v-for="date in visibleDates"
          :key="date"
          type="button"
          class="flex flex-col items-center rounded-xl px-4 py-2 min-w-[72px] transition-all duration-200 cursor-pointer"
          :class="
            date === selectedDate
              ? 'bg-primary text-white shadow-sm'
              : 'text-text-muted hover:bg-background-alt'
          "
          @click="selectDate(date)"
        >
          <span class="text-sm font-semibold">{{ formatDisplayDate(date) }}</span>
          <span v-if="isToday(date)" class="text-xs mt-0.5 opacity-80">今天</span>
        </button>
      </div>

      <button
        type="button"
        class="flex h-10 w-10 items-center justify-center rounded-xl text-text-muted hover:bg-background-alt hover:text-text transition-colors disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
        :disabled="currentIndex >= dates.length - 1"
        aria-label="下一天"
        @click="goNext"
      >
        <ChevronRight class="h-5 w-5" />
      </button>
    </div>
  </div>
</template>
