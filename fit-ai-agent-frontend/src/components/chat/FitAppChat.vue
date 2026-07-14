<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { History, MessageSquarePlus, X } from 'lucide-vue-next'
import ChatMessageList from './ChatMessageList.vue'
import ChatInput from './ChatInput.vue'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { useFitAppChat } from '@/composables/useFitAppChat'
import { useToast } from '@/composables/useToast'

const chatStore = useChatStore()
const auth = useAuthStore()
const {
  fitappMessages,
  isStreaming,
  historyLoading,
  fitappSessions,
  fitappChatId,
  sessionsLoading,
} = storeToRefs(chatStore)
const { success, error: showError } = useToast()

const showHistory = ref(false)

const { send, stop } = useFitAppChat(fitappMessages, isStreaming, () => chatStore.ensureChatId())

const currentTitle = computed(() => {
  const hit = fitappSessions.value.find((s) => s.chatId === fitappChatId.value)
  return hit?.title || '当前对话'
})

onMounted(async () => {
  if (!auth.user) {
    try {
      await auth.fetchCurrentUser()
    } catch {
      showError('请先登录后再使用健康咨询')
      return
    }
  }
  chatStore.bindCurrentUser()
  try {
    await Promise.all([chatStore.loadFitAppHistory(true), chatStore.loadFitAppSessions()])
  } catch (e) {
    showError(e instanceof Error ? e.message : '加载对话失败')
  }
})

function handleSend(message: string) {
  send(message)
  // 发送后刷新会话列表（标题可能更新）
  window.setTimeout(() => {
    void chatStore.loadFitAppSessions()
  }, 1500)
}

function handleNewChat() {
  if (isStreaming.value) {
    stop()
  }
  if (fitappMessages.value.length === 0) {
    success('已是新的咨询窗口')
    return
  }
  chatStore.startNewFitAppChat()
  showHistory.value = false
  success('已开启新的咨询对话')
}

async function handleSelectSession(chatId: string) {
  if (isStreaming.value) {
    showError('请等待当前回复结束后再切换')
    return
  }
  try {
    await chatStore.switchFitAppChat(chatId)
    showHistory.value = false
  } catch (e) {
    showError(e instanceof Error ? e.message : '切换对话失败')
  }
}

async function openHistory() {
  showHistory.value = true
  try {
    await chatStore.loadFitAppSessions()
  } catch (e) {
    showError(e instanceof Error ? e.message : '加载历史失败')
  }
}

function formatTime(value?: string) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <div class="card relative flex flex-col h-[calc(100dvh-180px)] min-h-[400px] overflow-hidden">
    <div class="flex items-center justify-between gap-3 border-b border-border/70 px-4 py-2.5">
      <div class="min-w-0">
        <p class="text-sm font-medium text-text">健康咨询</p>
        <p class="truncate text-xs text-text-muted">{{ currentTitle }}</p>
      </div>
      <div class="flex shrink-0 items-center gap-2">
        <button
          type="button"
          class="inline-flex items-center gap-1.5 rounded-lg border border-border bg-white px-3 py-1.5 text-xs font-medium text-text transition hover:border-primary/40 hover:text-primary"
          @click="openHistory"
        >
          <History class="h-3.5 w-3.5" />
          历史对话
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-1.5 rounded-lg border border-border bg-white px-3 py-1.5 text-xs font-medium text-text transition hover:border-primary/40 hover:text-primary disabled:opacity-50"
          :disabled="historyLoading"
          @click="handleNewChat"
        >
          <MessageSquarePlus class="h-3.5 w-3.5" />
          新建对话
        </button>
      </div>
    </div>

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

    <!-- 历史会话面板 -->
    <div
      v-if="showHistory"
      class="absolute inset-0 z-20 flex justify-end bg-black/20"
      @click.self="showHistory = false"
    >
      <aside class="flex h-full w-[min(100%,320px)] flex-col border-l border-border bg-white shadow-xl">
        <div class="flex items-center justify-between border-b border-border px-4 py-3">
          <p class="text-sm font-medium text-text">我的历史对话</p>
          <button type="button" class="rounded-md p-1 text-text-muted hover:bg-background-alt" @click="showHistory = false">
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-2">
          <p v-if="sessionsLoading" class="px-2 py-6 text-center text-xs text-text-muted">加载中…</p>
          <p v-else-if="fitappSessions.length === 0" class="px-2 py-6 text-center text-xs text-text-muted">
            暂无历史对话
          </p>
          <button
            v-for="session in fitappSessions"
            :key="session.chatId"
            type="button"
            class="mb-1 w-full rounded-xl px-3 py-2.5 text-left transition hover:bg-background-alt"
            :class="session.chatId === fitappChatId ? 'bg-primary/5 ring-1 ring-primary/20' : ''"
            @click="handleSelectSession(session.chatId)"
          >
            <p class="truncate text-sm font-medium text-text">{{ session.title || '新对话' }}</p>
            <p class="mt-0.5 text-[11px] text-text-muted">{{ formatTime(session.updateTime) }}</p>
          </button>
        </div>
      </aside>
    </div>
  </div>
</template>
