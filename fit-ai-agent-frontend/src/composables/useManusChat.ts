import { onUnmounted } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { buildManusStreamUrl, parseManusStep } from '@/api/chat'
import type { ChatMessage } from '@/types/chat'

export function useManusChat(messages: { value: ChatMessage[] }, isStreaming: { value: boolean }) {
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
      steps: [],
    })

    isStreaming.value = true
    const url = buildManusStreamUrl(message.trim())
    eventSource = new EventSource(url)

    eventSource.onmessage = (e) => {
      const msg = messages.value.find((m) => m.id === assistantId)
      if (!msg) return

      const parsed = parseManusStep(e.data)
      if (parsed) {
        if (!msg.steps) msg.steps = []
        const existing = msg.steps.find((s) => s.step === parsed.step)
        if (existing) {
          existing.content = parsed.content
        } else {
          msg.steps.push(parsed)
        }
        msg.content = msg.steps.map((s) => `step${s.step}: ${s.content}`).join('\n')
      } else {
        msg.content += e.data
      }
    }

    eventSource.onerror = () => {
      const msg = messages.value.find((m) => m.id === assistantId)
      if (msg) {
        msg.status = 'complete'
        if (!msg.content && (!msg.steps || msg.steps.length === 0)) {
          msg.content = '回复中断，请重试'
          msg.status = 'error'
        }
      }
      stop()
    }
  }

  return { send, stop }
}
