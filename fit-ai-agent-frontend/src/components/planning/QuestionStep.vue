<script setup lang="ts">
import { ref } from 'vue'
import { Send } from 'lucide-vue-next'

defineProps<{
  question: string
  questionIndex: number | null
  loading?: boolean
}>()

const emit = defineEmits<{
  submit: [answer: string]
}>()

const answer = ref('')

function handleSubmit() {
  if (!answer.value.trim()) return
  emit('submit', answer.value.trim())
  answer.value = ''
}
</script>

<template>
  <div class="card p-6">
    <div class="mb-6">
      <span class="inline-flex items-center rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
        问题 {{ questionIndex ?? 1 }} / 4
      </span>
      <h3 class="mt-3 font-heading text-lg font-semibold text-text leading-relaxed">
        {{ question }}
      </h3>
    </div>

    <form class="flex gap-3" @submit.prevent="handleSubmit">
      <input
        v-model="answer"
        type="text"
        class="input-field flex-1"
        placeholder="输入你的回答…"
        :disabled="loading"
        autofocus
      />
      <button type="submit" class="btn-primary shrink-0" :disabled="loading || !answer.trim()">
        <Send class="h-4 w-4" />
        发送
      </button>
    </form>
  </div>
</template>
