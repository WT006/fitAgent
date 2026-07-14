import type { TaskType } from '@/types/plan'

export interface TaskTypeMeta {
  label: string
  colorClass: string
  bgClass: string
}

const TASK_TYPE_MAP: Record<TaskType, TaskTypeMeta> = {
  SPORT: { label: '运动', colorClass: 'text-emerald-700', bgClass: 'bg-emerald-100' },
  DIET: { label: '饮食', colorClass: 'text-amber-700', bgClass: 'bg-amber-100' },
  REST: { label: '休息', colorClass: 'text-blue-700', bgClass: 'bg-blue-100' },
  HABIT: { label: '习惯', colorClass: 'text-violet-700', bgClass: 'bg-violet-100' },
}

export function getTaskTypeMeta(type: TaskType): TaskTypeMeta {
  return TASK_TYPE_MAP[type]
}
