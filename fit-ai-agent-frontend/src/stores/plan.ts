import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as planApi from '@/api/plan'
import type { PlanDayTasksVO, PlanProgressVO } from '@/types/plan'

export const usePlanStore = defineStore('plan', () => {
  const progress = ref<PlanProgressVO | null>(null)
  const dayTasks = ref<PlanDayTasksVO | null>(null)
  const loading = ref(false)

  async function fetchProgress() {
    loading.value = true
    try {
      progress.value = await planApi.getCurrentPlan()
      return progress.value
    } finally {
      loading.value = false
    }
  }

  async function fetchTasks(date: string) {
    loading.value = true
    try {
      dayTasks.value = await planApi.getTasksByDate(date)
      return dayTasks.value
    } finally {
      loading.value = false
    }
  }

  async function toggleTask(taskId: string) {
    const updated = await planApi.toggleTask(taskId)
    if (dayTasks.value) {
      const index = dayTasks.value.tasks.findIndex((t) => t.id === taskId)
      if (index !== -1) {
        dayTasks.value.tasks[index] = updated
      }
    }
    return updated
  }

  return {
    progress,
    dayTasks,
    loading,
    fetchProgress,
    fetchTasks,
    toggleTask,
  }
})
