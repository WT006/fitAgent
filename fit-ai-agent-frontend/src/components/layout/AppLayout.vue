<script setup lang="ts">
import AppNav from './AppNav.vue'

withDefaults(
  defineProps<{
    /** 管理页等需要更宽内容区时开启 */
    wide?: boolean
  }>(),
  { wide: false },
)
</script>

<template>
  <div class="min-h-dvh flex flex-col">
    <AppNav />
    <main class="flex-1 px-4 py-6">
      <div class="mx-auto" :class="wide ? 'max-w-5xl' : 'max-w-2xl'">
        <slot />
      </div>
    </main>
  </div>
</template>
