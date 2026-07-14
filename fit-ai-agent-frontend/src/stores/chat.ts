import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFitAppChatHistory } from '@/api/chat'
import type { ChatHistoryMessage, ChatMessage, ChatTab } from '@/types/chat'
import { getFitAppChatId, resetFitAppChatId } from '@/utils/chatId'

export const useChatStore = defineStore('chat', () => {
  const activeTab = ref<ChatTab>('fitapp')
  const fitappMessages = ref<ChatMessage[]>([])
  const manusMessages = ref<ChatMessage[]>([])
  const fitappChatId = ref(getFitAppChatId())
  const isStreaming = ref(false)
  const historyLoaded = ref(false)
  const historyLoading = ref(false)

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

  async function loadFitAppHistory(force = false) {
    if (isStreaming.value) return
    if (historyLoaded.value && !force) return
    if (historyLoading.value) return

    historyLoading.value = true
    try {
      const list = await getFitAppChatHistory(fitappChatId.value)
      fitappMessages.value = mapHistory(list || [])
      historyLoaded.value = true
    } finally {
      historyLoading.value = false
    }
  }

  function startNewFitAppChat() {
    fitappChatId.value = resetFitAppChatId()
    fitappMessages.value = []
    historyLoaded.value = true
  }

  return {
    activeTab,
    fitappMessages,
    manusMessages,
    fitappChatId,
    isStreaming,
    historyLoaded,
    historyLoading,
    setActiveTab,
    getMessages,
    loadFitAppHistory,
    startNewFitAppChat,
  }
})
