<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PlanPreview from './PlanPreview.vue'
import { usePlanning } from '@/composables/usePlanning'

const props = defineProps<{
  isReplan?: boolean
}>()

const router = useRouter()
const {
  phase,
  question,
  questionIndex,
  preview,
  loading,
  confirming,
  initPlanning,
  startReplan,
  submitAnswer,
  confirmPlan,
} = usePlanning()

onMounted(async () => {
  if (props.isReplan) {
    await startReplan()
  } else {
    await initPlanning()
  }
})

function handleBack() {
  router.push('/checkin')
}
</script>

<template>
  <PlanPreview
    :phase="phase"
    :question="question"
    :question-index="questionIndex"
    :preview="preview"
    :loading="loading"
    :confirming="confirming"
    @submit="submitAnswer"
    @confirm="confirmPlan"
    @back="handleBack"
  />
</template>
