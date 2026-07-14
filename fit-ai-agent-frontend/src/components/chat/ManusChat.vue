<script setup lang="ts">
import { storeToRefs } from 'pinia'
import ChatMessageList from './ChatMessageList.vue'
import ChatInput from './ChatInput.vue'
import { useChatStore } from '@/stores/chat'
import { useManusChat } from '@/composables/useManusChat'
import { Info } from 'lucide-vue-next'

const chatStore = useChatStore()
const { manusMessages, isStreaming } = storeToRefs(chatStore)

const { send, stop } = useManusChat(manusMessages, isStreaming)

function handleSend(message: string) {
  send(message)
}
</script>

<template>
  <div class="space-y-3">
    <div class="flex items-center gap-2 rounded-xl bg-amber-50 border border-amber-200 px-4 py-2.5 text-xs text-amber-800">
      <Info class="h-4 w-4 shrink-0" />
      智能体模式为单轮任务，每次请求独立执行
    </div>
    <div class="card flex flex-col h-[calc(100dvh-220px)] min-h-[400px] overflow-hidden">
      <ChatMessageList :messages="manusMessages" />
      <ChatInput
        :is-streaming="isStreaming"
        placeholder="描述你的任务，如：搜索最新减脂研究…"
        @send="handleSend"
        @stop="stop"
      />
    </div>
  </div>
</template>
