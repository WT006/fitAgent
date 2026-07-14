<script setup lang="ts">
import { computed, ref } from 'vue'
import { Download, FileText, Loader2 } from 'lucide-vue-next'
import type { ChatMessage } from '@/types/chat'
import StreamingIndicator from './StreamingIndicator.vue'
import ToolCallBadge from './ToolCallBadge.vue'
import { collectPdfDownloadsFromMessage } from '@/utils/pdfDownload'
import { getPdfDownloadUrl } from '@/api/files'
import { useToast } from '@/composables/useToast'

const props = defineProps<{
  message: ChatMessage
}>()

const { error: showError, success } = useToast()
const downloadingKey = ref<string | null>(null)

const pdfFiles = computed(() =>
  collectPdfDownloadsFromMessage(props.message.content, props.message.steps),
)

const processSteps = computed(() => {
  if (!props.message.steps?.length) return []
  // 有 PDF 下载区时，不在步骤里重复堆 PDF 成功原文
  return props.message.steps.filter((s) => !/downloadKey=/i.test(s.content))
})

async function handleDownload(downloadKey: string) {
  if (downloadingKey.value) return
  downloadingKey.value = downloadKey
  try {
    const data = await getPdfDownloadUrl(downloadKey)
    const link = document.createElement('a')
    link.href = data.url
    link.target = '_blank'
    link.rel = 'noopener'
    if (data.fileName) {
      link.download = data.fileName
    }
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    success('已开始下载 PDF')
  } catch (e) {
    showError(e instanceof Error ? e.message : '下载失败，请先登录后重试')
  } finally {
    downloadingKey.value = null
  }
}
</script>

<template>
  <div
    class="flex"
    :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
  >
    <div
      class="max-w-[85%] space-y-2"
      :class="message.role === 'user' ? 'items-end' : 'items-start'"
    >
      <div
        v-if="message.role === 'user' || (!processSteps.length && !pdfFiles.length)"
        class="rounded-2xl px-4 py-3 text-sm leading-relaxed whitespace-pre-wrap"
        :class="
          message.role === 'user'
            ? 'bg-primary text-white rounded-br-md'
            : 'bg-white border border-border text-text rounded-bl-md shadow-sm'
        "
      >
        {{ message.content }}
        <StreamingIndicator v-if="message.status === 'streaming'" />
      </div>

      <template v-else>
        <div
          v-if="processSteps.length"
          class="rounded-2xl border border-border bg-white px-3 py-2 shadow-sm rounded-bl-md space-y-1.5"
        >
          <p class="px-1 text-[11px] text-text-muted">执行过程</p>
          <ToolCallBadge
            v-for="step in processSteps"
            :key="step.step"
            :step="step.step"
            :content="step.content"
          />
          <StreamingIndicator v-if="message.status === 'streaming'" class="px-1" />
        </div>

        <div
          v-for="file in pdfFiles"
          :key="file.downloadKey"
          class="rounded-2xl border border-primary/25 bg-white px-4 py-3 shadow-sm rounded-bl-md"
        >
          <div class="flex items-start gap-3">
            <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <FileText class="h-5 w-5" />
            </div>
            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-text">已为你生成PDF</p>
              <p class="mt-0.5 truncate text-xs text-text-muted">{{ file.displayName }}</p>
              <button
                type="button"
                class="mt-3 inline-flex items-center gap-2 rounded-lg bg-primary px-3 py-2 text-xs font-medium text-white transition hover:opacity-90 disabled:opacity-60"
                :disabled="!!downloadingKey"
                @click="handleDownload(file.downloadKey)"
              >
                <Loader2 v-if="downloadingKey === file.downloadKey" class="h-3.5 w-3.5 animate-spin" />
                <Download v-else class="h-3.5 w-3.5" />
                下载 PDF
              </button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
