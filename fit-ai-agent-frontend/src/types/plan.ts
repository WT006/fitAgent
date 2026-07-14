export type TaskType = 'SPORT' | 'DIET' | 'REST' | 'HABIT'
export type TaskStatus = 0 | 1

export interface PlanProgressVO {
  /** 雪花 ID，以后端字符串序列化为准 */
  planId: string | null
  startDate: string | null
  currentDay: number
  totalDays: number
  progressPercent: number
  hasPlan: boolean
}

export interface DailyTaskVO {
  /** 雪花 ID，以后端字符串序列化为准 */
  id: string
  dayIndex: number
  title: string
  type: TaskType
  status: TaskStatus
  completedAt: string | null
}

export interface PlanDayTasksVO {
  date: string
  dayIndex: number
  canToggle: boolean
  tasks: DailyTaskVO[]
}

export interface PreviewTask {
  title: string
  type: TaskType
}

export interface PreviewDay {
  dayIndex: number
  tasks: PreviewTask[]
}

export interface PlanPreviewVO {
  days: PreviewDay[]
}

export interface PlanAiChatVO {
  phase: 'question' | 'preview'
  questionIndex: number | null
  question: string | null
  preview: PlanPreviewVO | null
}

export type PlanningPhase = 'question' | 'preview' | 'generating'
