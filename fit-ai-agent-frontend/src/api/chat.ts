import request, { getToken } from './request'
import type { ChatHistoryMessage, ChatSessionItem } from '@/types/chat'

export function buildFitAppStreamUrl(message: string, chatId: string): string {
  const params = new URLSearchParams({ message, chatId })
  const token = getToken()
  if (token) {
    params.set('token', token)
  }
  return `/api/fitai/fitapp/chat/stream?${params.toString()}`
}

export function getFitAppChatHistory(chatId: string) {
  return request.get<unknown, ChatHistoryMessage[]>('/fitai/fitapp/chat/history', {
    params: { chatId },
  })
}

export function getFitAppChatSessions() {
  return request.get<unknown, ChatSessionItem[]>('/fitai/fitapp/chat/sessions')
}

export function buildManusStreamUrl(message: string): string {
  const params = new URLSearchParams({ message })
  return `/api/fitai/manus/chat?${params.toString()}`
}

export function parseManusStep(data: string): { step: number; content: string } | null {
  const match = data.match(/^step(\d+):\s*(.*)$/s)
  if (!match) return null
  return { step: parseInt(match[1], 10), content: match[2] }
}
