import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFitAppChatHistory, getFitAppChatSessions } from '@/api/chat'
import type { ChatHistoryMessage, ChatMessage, ChatSessionItem, ChatTab } from '@/types/chat'
import { getFitAppChatId, resetFitAppChatId, setFitAppChatId } from '@/utils/chatId'
import { useAuthStore } from '@/stores/auth'

export const useChatStore = defineStore('chat', () => {
  const activeTab = ref<ChatTab>('fitapp')
  const fitappMessages = ref<ChatMessage[]>([])
  const manusMessages = ref<ChatMessage[]>([])
  const fitappChatId = ref('')
  const fitappSessions = ref<ChatSessionItem[]>([])
  const isStreaming = ref(false)
  const historyLoaded = ref(false)
  const historyLoading = ref(false)
  const sessionsLoading = ref(false)

  function currentUserId() {
    return useAuthStore().user?.id ?? null
  }

  function ensureChatId() {
    if (!fitappChatId.value) {
      fitappChatId.value = getFitAppChatId(currentUserId())
    }
    return fitappChatId.value
  }

  function setActiveTab(tab: ChatTab) {
    activeTab.value = tab
  }

  function getMessages(tab: ChatTab) {
    return tab === 'fitapp' ? fitappMessages : manusMessages
  }

  function mapHistory(items: ChatHistoryMessage[]): ChatMessage[] {
    return items.map((item) => ({
      id: item.id,
      role: item.role,
      content: item.content ?? '',
      status: 'complete' as const,
    }))
  }

  async function loadFitAppSessions() {
    sessionsLoading.value = true
    try {
      fitappSessions.value = (await getFitAppChatSessions()) || []
    } finally {
      sessionsLoading.value = false
    }
  }

  async function loadFitAppHistory(force = false) {
    if (isStreaming.value) return
    if (historyLoaded.value && !force) return
    if (historyLoading.value) return

    ensureChatId()
    historyLoading.value = true
    try {
      const list = await getFitAppChatHistory(fitappChatId.value)
      fitappMessages.value = mapHistory(list || [])
      historyLoaded.value = true
    } finally {
      historyLoading.value = false
    }
  }

  async function switchFitAppChat(chatId: string) {
    if (!chatId || chatId === fitappChatId.value) return
    if (isStreaming.value) return
    fitappChatId.value = chatId
    setFitAppChatId(chatId, currentUserId())
    historyLoaded.value = false
    await loadFitAppHistory(true)
  }

  function startNewFitAppChat() {
    fitappChatId.value = resetFitAppChatId(currentUserId())
    fitappMessages.value = []
    historyLoaded.value = true
  }

  /** 登录用户变化时切换到该用户自己的会话 */
  function bindCurrentUser() {
    fitappChatId.value = getFitAppChatId(currentUserId())
    fitappMessages.value = []
    fitappSessions.value = []
    historyLoaded.value = false
  }

  function resetOnLogout() {
    fitappMessages.value = []
    fitappSessions.value = []
    fitappChatId.value = ''
    historyLoaded.value = false
  }

  return {
    activeTab,
    fitappMessages,
    manusMessages,
    fitappChatId,
    fitappSessions,
    isStreaming,
    historyLoaded,
    historyLoading,
    sessionsLoading,
    setActiveTab,
    getMessages,
    ensureChatId,
    loadFitAppHistory,
    loadFitAppSessions,
    switchFitAppChat,
    startNewFitAppChat,
    bindCurrentUser,
    resetOnLogout,
  }
})
