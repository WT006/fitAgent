<script setup lang="ts">
import { computed } from 'vue'
import { Search, FileText, Loader2, Wrench, CheckCircle2, XCircle } from 'lucide-vue-next'

const props = defineProps<{
  step: number
  content: string
}>()

const toolName = computed(() => {
  const toolMatch = props.content.match(/(?:工具\s+|tool=)([^\s，,=]+)/i)
  if (toolMatch) return toolMatch[1]
  if (/downloadKey=/i.test(props.content)) return 'generatePDF'
  return `Step ${props.step}`
})

const status = computed<'loading' | 'done' | 'failed' | 'pdf'>(() => {
  const text = props.content.toLowerCase()
  if (text.includes('downloadkey=')) return 'pdf'
  if (text.includes('status=failed') || text.includes('error') || text.includes('失败')) return 'failed'
  if (text.includes('status=done') || text.includes('完成') || text.includes('success') || text.includes('generated')) {
    return 'done'
  }
  return 'loading'
})

const label = computed(() => {
  if (status.value === 'pdf') return 'PDF 已生成'
  if (status.value === 'failed') return `${toolName.value} 失败`
  if (status.value === 'done') return `${toolName.value} 已完成`
  return `${toolName.value} 执行中…`
})

const icon = computed(() => {
  if (status.value === 'pdf') return FileText
  if (status.value === 'failed') return XCircle
  if (status.value === 'done') return CheckCircle2
  const name = toolName.value.toLowerCase()
  if (name.includes('search') || name.includes('map')) return Search
  if (name.includes('pdf')) return FileText
  if (status.value === 'loading') return Loader2
  return Wrench
})
</script>

<template>
  <div class="flex items-center gap-2 rounded-lg bg-background-alt px-3 py-2 text-xs">
    <component
      :is="icon"
      class="h-3.5 w-3.5 shrink-0"
      :class="{
        'animate-spin text-primary': status === 'loading',
        'text-primary': status === 'done' || status === 'pdf',
        'text-red-500': status === 'failed',
      }"
    />
    <span class="font-medium text-text-secondary">{{ label }}</span>
  </div>
</template>
