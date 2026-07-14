<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import ChatTabs from '@/components/chat/ChatTabs.vue'
import FitAppChat from '@/components/chat/FitAppChat.vue'
import ManusChat from '@/components/chat/ManusChat.vue'
import PlanningWizard from '@/components/planning/PlanningWizard.vue'
import { useChatStore } from '@/stores/chat'
import { storeToRefs } from 'pinia'

const route = useRoute()
const chatStore = useChatStore()
const { activeTab } = storeToRefs(chatStore)

const isPlanningMode = computed(() => route.query.mode === 'planning')
const isReplan = computed(() => route.query.replan === '1')

function handleTabChange(tab: 'fitapp' | 'manus') {
  chatStore.setActiveTab(tab)
  chatStore.isStreaming = false
}

watch(isPlanningMode, (planning) => {
  if (!planning) {
    chatStore.isStreaming = false
  }
})
</script>

<template>
  <AppLayout>
    <PlanningWizard v-if="isPlanningMode" :is-replan="isReplan" />

    <div v-else class="space-y-4">
      <ChatTabs :active-tab="activeTab" @change="handleTabChange" />
      <FitAppChat v-if="activeTab === 'fitapp'" />
      <ManusChat v-else />
    </div>
  </AppLayout>
</template>
