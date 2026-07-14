import { v4 as uuidv4 } from 'uuid'

const LEGACY_KEY = 'fitapp_chat_id'

function storageKey(userId?: string | number | null) {
  if (userId == null || userId === '') {
    return LEGACY_KEY
  }
  return `fitapp_chat_id_${userId}`
}

export function generateChatId(): string {
  return uuidv4()
}

/**
 * 按用户隔离的健康咨询会话 ID
 */
export function getFitAppChatId(userId?: string | number | null): string {
  const key = storageKey(userId)
  let chatId = localStorage.getItem(key)
  if (!chatId && userId != null) {
    // 兼容未按用户隔离的旧 key（仅迁移给当前用户一次）
    const legacy = localStorage.getItem(LEGACY_KEY) || sessionStorage.getItem(LEGACY_KEY)
    if (legacy) {
      chatId = legacy
      localStorage.setItem(key, chatId)
      localStorage.removeItem(LEGACY_KEY)
      sessionStorage.removeItem(LEGACY_KEY)
    }
  }
  if (!chatId) {
    chatId = generateChatId()
    localStorage.setItem(key, chatId)
  }
  return chatId
}

export function setFitAppChatId(chatId: string, userId?: string | number | null): void {
  localStorage.setItem(storageKey(userId), chatId)
}

export function resetFitAppChatId(userId?: string | number | null): string {
  const chatId = generateChatId()
  setFitAppChatId(chatId, userId)
  return chatId
}

export function clearFitAppChatId(userId?: string | number | null): void {
  localStorage.removeItem(storageKey(userId))
  localStorage.removeItem(LEGACY_KEY)
  sessionStorage.removeItem(LEGACY_KEY)
}
