<script setup lang="ts">
import { useRouter } from 'vue-router'
import { MessageCircle, Bot, ChevronRight } from 'lucide-vue-next'

const router = useRouter()

const entries = [
  {
    id: 'consult',
    path: '/chat/consult',
    title: '健康咨询',
    badge: '多轮对话',
    desc: '像私教一样陪你聊运动、饮食与作息。它记得你的目标和困扰，持续给出贴合你的建议。',
    tone: 'mint' as const,
    icon: MessageCircle,
  },
  {
    id: 'agent',
    path: '/chat/agent',
    title: '超级智能体',
    badge: '会做事',
    desc: '告诉它城市位置和具体需求，它会联网检索、分步规划，还能为你生成可下载的锻炼计划 PDF。',
    tone: 'teal' as const,
    icon: Bot,
  },
]

function go(path: string) {
  router.push(path)
}
</script>

<template>
  <div class="chat-hub space-y-5 pb-2">
    <header class="px-1 pt-1">
      <h1 class="font-heading text-2xl font-bold tracking-tight text-text">选一种对话方式</h1>
      <p class="mt-1.5 text-sm text-text-muted">点进对应工作台，开始你的今日训练助理</p>
    </header>

    <button
      v-for="(item, index) in entries"
      :key="item.id"
      type="button"
      class="jelly-card group w-full text-left"
      :class="item.tone === 'mint' ? 'jelly-mint' : 'jelly-teal'"
      :style="{ animationDelay: `${index * 80}ms` }"
      @click="go(item.path)"
    >
      <div class="jelly-sheen" aria-hidden="true" />
      <div class="relative flex items-start gap-4 p-5 sm:p-6">
        <div
          class="jelly-icon flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl"
          :class="item.tone === 'mint' ? 'bg-primary/15 text-primary' : 'bg-teal/15 text-teal'"
        >
          <component :is="item.icon" class="h-7 w-7" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="flex items-center gap-2">
            <h2 class="font-heading text-xl font-semibold text-text">{{ item.title }}</h2>
            <span class="jelly-badge">{{ item.badge }}</span>
          </div>
          <p class="mt-2 text-sm leading-relaxed text-text-secondary/90">
            {{ item.desc }}
          </p>
          <p
            class="mt-4 inline-flex items-center gap-1 text-sm font-semibold transition-transform duration-300 group-hover:translate-x-0.5"
            :class="item.tone === 'mint' ? 'text-primary' : 'text-teal'"
          >
            进入
            <ChevronRight class="h-4 w-4" />
          </p>
        </div>
      </div>
    </button>
  </div>
</template>
