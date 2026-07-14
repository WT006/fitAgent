<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Sparkles, Loader2 } from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import ProgressBar from '@/components/checkin/ProgressBar.vue'
import DateNavigator from '@/components/checkin/DateNavigator.vue'
import TaskList from '@/components/checkin/TaskList.vue'
import EmptyPlan from '@/components/checkin/EmptyPlan.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { usePlanStore } from '@/stores/plan'
import { useToast } from '@/composables/useToast'
import { today, getDateRange } from '@/utils/date'

const router = useRouter()
const planStore = usePlanStore()
const { error: showError } = useToast()

const selectedDate = ref(today())
const togglingId = ref<string | null>(null)
const showReplanDialog = ref(false)
const pageLoading = ref(true)

const progress = computed(() => planStore.progress)
const dayTasks = computed(() => planStore.dayTasks)

const dateRange = computed(() => {
  if (!progress.value?.startDate) return []
  return getDateRange(progress.value.startDate, progress.value.totalDays)
})

async function loadPage() {
  pageLoading.value = true
  try {
    await planStore.fetchProgress()
    if (progress.value?.hasPlan && progress.value.startDate) {
      selectedDate.value = today()
      await planStore.fetchTasks(selectedDate.value)
    }
  } catch (e) {
    showError(e instanceof Error ? e.message : '加载失败')
  } finally {
    pageLoading.value = false
  }
}

async function handleDateChange(date: string) {
  selectedDate.value = date
  try {
    await planStore.fetchTasks(date)
  } catch (e) {
    showError(e instanceof Error ? e.message : '加载任务失败')
  }
}

async function handleToggle(taskId: string) {
  if (!dayTasks.value?.canToggle) return
  togglingId.value = taskId
  const task = dayTasks.value.tasks.find((t) => t.id === taskId)
  if (!task) return

  const prevStatus = task.status
  task.status = prevStatus === 1 ? 0 : 1

  try {
    await planStore.toggleTask(taskId)
  } catch (e) {
    task.status = prevStatus
    showError(e instanceof Error ? e.message : '操作失败')
  } finally {
    togglingId.value = null
  }
}

function handlePlanClick() {
  if (progress.value?.hasPlan) {
    showReplanDialog.value = true
  } else {
    router.push('/chat/consult?mode=planning')
  }
}

function handleReplanConfirm() {
  showReplanDialog.value = false
  router.push('/chat/consult?mode=planning&replan=1')
}

onMounted(loadPage)

watch(
  () => router.currentRoute.value.fullPath,
  (path) => {
    if (path.startsWith('/checkin')) {
      loadPage()
    }
  },
)
</script>

<template>
  <AppLayout>
    <div v-if="pageLoading" class="flex justify-center py-20">
      <Loader2 class="h-8 w-8 animate-spin text-primary" />
    </div>

    <template v-else>
      <div class="space-y-4">
        <ProgressBar v-if="progress" :progress="progress" />

        <template v-if="progress?.hasPlan">
          <DateNavigator
            :dates="dateRange"
            :selected-date="selectedDate"
            @change="handleDateChange"
          />
          <TaskList
            v-if="dayTasks"
            :tasks="dayTasks.tasks"
            :can-toggle="dayTasks.canToggle"
            :day-index="dayTasks.dayIndex"
            :toggling-id="togglingId"
            @toggle="handleToggle"
          />
        </template>

        <EmptyPlan v-else @plan="handlePlanClick" />

        <div v-if="progress?.hasPlan" class="pt-2 pb-4">
          <button type="button" class="btn-primary w-full py-3" @click="handlePlanClick">
            <Sparkles class="h-5 w-5" />
            AI 帮我规划
          </button>
        </div>
      </div>
    </template>

    <ConfirmDialog
      v-if="showReplanDialog"
      title="重新规划"
      message="重新规划将清除当前计划及所有打卡记录，从今天起生成新的 30 天计划。确定继续吗？"
      confirm-text="确认重新规划"
      cancel-text="取消"
      destructive
      @confirm="handleReplanConfirm"
      @cancel="showReplanDialog = false"
    />
  </AppLayout>
</template>
