import { onUnmounted } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { buildFitAppStreamUrl } from '@/api/chat'
import type { ChatMessage } from '@/types/chat'

export function useFitAppChat(
  messages: { value: ChatMessage[] },
  isStreaming: { value: boolean },
  getChatId: () => string,
) {
  let eventSource: EventSource | null = null

  function stop() {
    eventSource?.close()
    eventSource = null
    isStreaming.value = false
  }

  onUnmounted(stop)

  function send(message: string) {
    if (!message.trim() || isStreaming.value) return

    messages.value.push({
      id: uuidv4(),
      role: 'user',
      content: message.trim(),
      status: 'complete',
    })

    const assistantId = uuidv4()
    messages.value.push({
      id: assistantId,
      role: 'assistant',
      content: '',
      status: 'streaming',
    })

    isStreaming.value = true
    const url = buildFitAppStreamUrl(message.trim(), getChatId())
    eventSource = new EventSource(url)

    eventSource.onmessage = (e) => {
      const msg = messages.value.find((m) => m.id === assistantId)
      if (msg) {
        msg.content += e.data
      }
    }

    eventSource.onerror = () => {
      const msg = messages.value.find((m) => m.id === assistantId)
      if (msg) {
        msg.status = 'complete'
        if (!msg.content) {
          msg.content = '回复中断，请先登录后重试'
          msg.status = 'error'
        }
      }
      stop()
    }
  }

  return { send, stop }
}
