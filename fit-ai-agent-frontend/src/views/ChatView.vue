<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import ChatModeHub from '@/components/chat/ChatModeHub.vue'
import FitAppChat from '@/components/chat/FitAppChat.vue'
import ManusChat from '@/components/chat/ManusChat.vue'
import PlanningWizard from '@/components/planning/PlanningWizard.vue'
import { useChatStore } from '@/stores/chat'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()

const isPlanningMode = computed(() => route.query.mode === 'planning')
const isReplan = computed(() => route.query.replan === '1')
const mode = computed(() => {
  if (route.path.endsWith('/consult')) return 'consult'
  if (route.path.endsWith('/agent')) return 'agent'
  return 'hub'
})

const pageTitle = computed(() => {
  if (mode.value === 'consult') return '健康咨询'
  if (mode.value === 'agent') return '超级智能体'
  return ''
})

watch(
  () => route.fullPath,
  () => {
    chatStore.isStreaming = false
  },
)

function goBack() {
  router.push('/chat')
}
</script>

<template>
  <AppLayout>
    <PlanningWizard v-if="isPlanningMode" :is-replan="isReplan" />

    <template v-else>
      <ChatModeHub v-if="mode === 'hub'" />

      <div v-else class="space-y-3">
        <button
          type="button"
          class="inline-flex items-center gap-1.5 rounded-xl px-1 py-1 text-sm font-medium text-text-secondary transition hover:text-primary"
          @click="goBack"
        >
          <ArrowLeft class="h-4 w-4" />
          返回选择
          <span class="text-text-muted">·</span>
          <span class="text-text">{{ pageTitle }}</span>
        </button>

        <FitAppChat v-if="mode === 'consult'" />
        <ManusChat v-else />
      </div>
    </template>
  </AppLayout>
</template>
