import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { PlanningPhase, PlanAiChatVO, PreviewDay } from '@/types/plan'
import * as planApi from '@/api/plan'
import { useToast } from './useToast'

export function usePlanning() {
  const router = useRouter()
  const { error, success } = useToast()

  const phase = ref<PlanningPhase>('question')
  const questionIndex = ref<number | null>(null)
  const question = ref<string | null>(null)
  const preview = ref<PreviewDay[] | null>(null)
  const answers = ref<string[]>([])
  const loading = ref(false)
  const confirming = ref(false)

  function applyChatResponse(data: PlanAiChatVO) {
    if (data.phase === 'preview' && data.preview) {
      phase.value = 'preview'
      questionIndex.value = null
      question.value = null
      preview.value = data.preview.days.map((d) => ({
        dayIndex: d.dayIndex,
        tasks: d.tasks.map((t) => ({ ...t })),
      }))
    } else {
      phase.value = 'question'
      questionIndex.value = data.questionIndex
      question.value = data.question
    }
  }

  async function initPlanning() {
    loading.value = true
    try {
      const previewData = await planApi.getPlanPreview()
      preview.value = previewData.days.map((d) => ({
        dayIndex: d.dayIndex,
        tasks: d.tasks.map((t) => ({ ...t })),
      }))
      phase.value = 'preview'
    } catch {
      try {
        // 无预览时恢复问答会话（含二次进入仍卡在某一题的情况）
        const data = await planApi.planAiChat()
        applyChatResponse(data)
      } catch (e) {
        error(e instanceof Error ? e.message : '加载规划失败，请重试')
      }
    } finally {
      loading.value = false
    }
  }

  async function startReplan() {
    loading.value = true
    try {
      const data = await planApi.replan()
      answers.value = []
      preview.value = null
      applyChatResponse(data)
    } catch (e) {
      error(e instanceof Error ? e.message : '重新规划失败')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function submitAnswer(message: string) {
    answers.value.push(message)
    phase.value = 'generating'
    loading.value = true
    try {
      const data = await planApi.planAiChat(message)
      applyChatResponse(data)
    } catch (e) {
      phase.value = 'question'
      answers.value.pop()
      error(e instanceof Error ? e.message : '提交失败')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function confirmPlan() {
    if (!preview.value) return
    confirming.value = true
    try {
      await planApi.confirmPlan(preview.value)
      success('计划已创建！')
      router.push('/checkin')
    } catch (e) {
      error(e instanceof Error ? e.message : '确认失败')
      throw e
    } finally {
      confirming.value = false
    }
  }

  function updateTaskTitle(dayIndex: number, taskIndex: number, title: string) {
    if (!preview.value) return
    const day = preview.value.find((d) => d.dayIndex === dayIndex)
    if (day) {
      day.tasks[taskIndex].title = title
    }
  }

  return {
    phase,
    questionIndex,
    question,
    preview,
    answers,
    loading,
    confirming,
    initPlanning,
    startReplan,
    submitAnswer,
    confirmPlan,
    updateTaskTitle,
  }
}
