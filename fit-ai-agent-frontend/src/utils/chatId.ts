import { v4 as uuidv4 } from 'uuid'

const FITAPP_CHAT_ID_KEY = 'fitapp_chat_id'

export function generateChatId(): string {
  return uuidv4()
}

/**
 * 健康咨询会话 ID：用 localStorage 跨刷新/关标签保持，便于恢复历史
 */
export function getFitAppChatId(): string {
  let chatId = localStorage.getItem(FITAPP_CHAT_ID_KEY)
  if (!chatId) {
    // 兼容旧版 sessionStorage
    chatId = sessionStorage.getItem(FITAPP_CHAT_ID_KEY)
    if (chatId) {
      localStorage.setItem(FITAPP_CHAT_ID_KEY, chatId)
      sessionStorage.removeItem(FITAPP_CHAT_ID_KEY)
    } else {
      chatId = generateChatId()
      localStorage.setItem(FITAPP_CHAT_ID_KEY, chatId)
    }
  }
  return chatId
}

export function resetFitAppChatId(): string {
  const chatId = generateChatId()
  localStorage.setItem(FITAPP_CHAT_ID_KEY, chatId)
  sessionStorage.removeItem(FITAPP_CHAT_ID_KEY)
  return chatId
}
