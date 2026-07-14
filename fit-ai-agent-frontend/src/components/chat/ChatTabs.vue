<script setup lang="ts">
import type { ChatTab } from '@/types/chat'
import { MessageCircle, Bot } from 'lucide-vue-next'

defineProps<{
  activeTab: ChatTab
}>()

const emit = defineEmits<{
  change: [tab: ChatTab]
}>()

const tabs = [
  { id: 'fitapp' as ChatTab, label: '健康咨询', icon: MessageCircle },
  { id: 'manus' as ChatTab, label: '智能体', icon: Bot },
]
</script>

<template>
  <div class="flex gap-1 p-1 rounded-xl bg-background-alt border border-border">
    <button
      v-for="tab in tabs"
      :key="tab.id"
      type="button"
      class="flex flex-1 items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition-all duration-200 cursor-pointer"
      :class="
        activeTab === tab.id
          ? 'bg-white text-primary shadow-sm'
          : 'text-text-muted hover:text-text'
      "
      @click="emit('change', tab.id)"
    >
      <component :is="tab.icon" class="h-4 w-4" />
      {{ tab.label }}
    </button>
  </div>
</template>
