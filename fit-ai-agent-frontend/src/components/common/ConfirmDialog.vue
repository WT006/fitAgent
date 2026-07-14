<script setup lang="ts">
import { ref } from 'vue'
import { AlertTriangle, X } from 'lucide-vue-next'

defineProps<{
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  destructive?: boolean
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const visible = ref(true)

function handleCancel() {
  visible.value = false
  emit('cancel')
}

function handleConfirm() {
  visible.value = false
  emit('confirm')
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-[200] flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
    >
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="handleCancel" />
      <div class="card relative w-full max-w-md p-6 animate-in fade-in zoom-in duration-200">
        <button
          type="button"
          class="absolute top-4 right-4 rounded-lg p-1 text-text-muted hover:bg-background-alt cursor-pointer transition-colors"
          aria-label="关闭"
          @click="handleCancel"
        >
          <X class="h-5 w-5" />
        </button>
        <div class="flex items-start gap-4">
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full"
            :class="destructive ? 'bg-red-100' : 'bg-amber-100'"
          >
            <AlertTriangle
              class="h-5 w-5"
              :class="destructive ? 'text-red-600' : 'text-amber-600'"
            />
          </div>
          <div class="flex-1 pt-0.5">
            <h3 class="font-heading text-lg font-semibold text-text">{{ title }}</h3>
            <p class="mt-2 text-sm text-text-muted leading-relaxed">{{ message }}</p>
            <div class="mt-6 flex gap-3 justify-end">
              <button type="button" class="btn-secondary" @click="handleCancel">
                {{ cancelText || '取消' }}
              </button>
              <button
                type="button"
                class="btn-primary"
                :class="{ '!bg-red-600 hover:!bg-red-700': destructive }"
                @click="handleConfirm"
              >
                {{ confirmText || '确认' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
