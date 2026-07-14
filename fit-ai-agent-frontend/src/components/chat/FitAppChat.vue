<script setup lang="ts">
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import ChatMessageList from './ChatMessageList.vue'
import ChatInput from './ChatInput.vue'
import { useChatStore } from '@/stores/chat'
import { useFitAppChat } from '@/composables/useFitAppChat'

const chatStore = useChatStore()
const { fitappMessages, isStreaming, historyLoading } = storeToRefs(chatStore)

const { send, stop } = useFitAppChat(fitappMessages, isStreaming)

onMounted(async () => {
  try {
    await chatStore.loadFitAppHistory()
  } catch {
    // 历史加载失败时仍可继续新对话
  }
})

function handleSend(message: string) {
  send(message)
}
</script>

<template>
  <div class="card flex flex-col h-[calc(100dvh-180px)] min-h-[400px] overflow-hidden">
    <div
      v-if="historyLoading && fitappMessages.length === 0"
      class="flex flex-1 items-center justify-center text-sm text-text-muted"
    >
      正在加载历史对话…
    </div>
    <ChatMessageList v-else :messages="fitappMessages" />
    <ChatInput
      :is-streaming="isStreaming"
      placeholder="咨询运动健康问题…"
      @send="handleSend"
      @stop="stop"
    />
  </div>
</template>
