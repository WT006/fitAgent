export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  status: 'complete' | 'streaming' | 'error'
  steps?: Array<{ step: number; content: string }>
}

/** 后端会话历史 */
export interface ChatHistoryMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  createTime: string
}

export type ChatTab = 'fitapp' | 'manus'
