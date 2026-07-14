<script setup lang="ts">
import ChatMessage from './ChatMessage.vue'
import type { ChatMessage as ChatMessageType } from '@/types/chat'
import { Bot } from 'lucide-vue-next'

defineProps<{
  messages: ChatMessageType[]
}>()
</script>

<template>
  <div class="flex-1 overflow-y-auto space-y-4 p-4 min-h-0">
    <div v-if="messages.length === 0" class="flex flex-col items-center justify-center py-16 text-center">
      <div class="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10">
        <Bot class="h-7 w-7 text-primary" />
      </div>
      <p class="text-sm text-text-muted">发送消息开始对话</p>
    </div>
    <ChatMessage v-for="msg in messages" :key="msg.id" :message="msg" />
  </div>
</template>
