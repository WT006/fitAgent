<script setup lang="ts">
import { CheckCircle, AlertCircle, Info } from 'lucide-vue-next'
import { toasts } from '@/composables/useToast'
</script>

<template>
  <div
    class="fixed top-4 right-4 z-[1000] flex flex-col gap-2 max-w-sm"
    aria-live="polite"
  >
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="flex items-center gap-3 rounded-xl px-4 py-3 shadow-md backdrop-blur-sm"
        :class="{
          'bg-emerald-50 border border-emerald-200 text-emerald-800': toast.type === 'success',
          'bg-red-50 border border-red-200 text-red-800': toast.type === 'error',
          'bg-white border border-border text-text': toast.type === 'info',
        }"
      >
        <CheckCircle v-if="toast.type === 'success'" class="h-5 w-5 shrink-0 text-emerald-600" />
        <AlertCircle v-else-if="toast.type === 'error'" class="h-5 w-5 shrink-0 text-red-600" />
        <Info v-else class="h-5 w-5 shrink-0 text-primary" />
        <span class="text-sm">{{ toast.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
