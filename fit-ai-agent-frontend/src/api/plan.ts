import request from './request'
import type {
  DailyTaskVO,
  PlanAiChatVO,
  PlanDayTasksVO,
  PlanPreviewVO,
  PlanProgressVO,
  PreviewDay,
} from '@/types/plan'

export function getCurrentPlan() {
  return request.get<unknown, PlanProgressVO>('/plan/current')
}

export function getTasksByDate(date: string) {
  return request.get<unknown, PlanDayTasksVO>('/plan/tasks', { params: { date } })
}

export function toggleTask(taskId: string) {
  return request.post<unknown, DailyTaskVO>('/plan/task/toggle', { taskId })
}

export function planAiChat(message?: string) {
  return request.post<unknown, PlanAiChatVO>('/plan/ai/chat', message ? { message } : {})
}

export function getPlanPreview() {
  return request.get<unknown, PlanPreviewVO>('/plan/ai/preview')
}

export function confirmPlan(days: PreviewDay[]) {
  return request.post<unknown, PlanProgressVO>('/plan/ai/confirm', { days })
}

export function replan() {
  return request.post<unknown, PlanAiChatVO>('/plan/ai/replan')
}
