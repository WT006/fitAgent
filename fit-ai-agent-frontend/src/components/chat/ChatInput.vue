<script setup lang="ts">
import { ref } from 'vue'
import { Send, Square } from 'lucide-vue-next'

defineProps<{
  disabled?: boolean
  isStreaming?: boolean
  placeholder?: string
}>()

const emit = defineEmits<{
  send: [message: string]
  stop: []
}>()

const input = ref('')

function handleSubmit() {
  if (!input.value.trim()) return
  emit('send', input.value)
  input.value = ''
}
</script>

<template>
  <div class="border-t border-border bg-white/80 backdrop-blur-sm p-4">
    <form class="flex gap-3" @submit.prevent="handleSubmit">
      <input
        v-model="input"
        type="text"
        class="input-field flex-1"
        :placeholder="placeholder || '输入消息…'"
        :disabled="disabled"
      />
      <button
        v-if="isStreaming"
        type="button"
        class="btn-secondary shrink-0"
        @click="emit('stop')"
      >
        <Square class="h-4 w-4" />
        停止
      </button>
      <button
        v-else
        type="submit"
        class="btn-primary shrink-0"
        :disabled="disabled || !input.trim()"
      >
        <Send class="h-4 w-4" />
        发送
      </button>
    </form>
  </div>
</template>
