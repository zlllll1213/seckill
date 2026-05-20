<template>
  <div class="countdown" :class="{ urgent: remaining <= 60 }">
    <template v-if="remaining > 0">
      <span class="label">{{ label }}</span>
      <span class="time">{{ formatted }}</span>
    </template>
    <span v-else class="label">活动已结束</span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  targetTime: { type: String, required: true },
  label: { type: String, default: '距结束' }
})

const emit = defineEmits(['ended'])

const remaining = ref(0)
let timer = null
let endedEmitted = false

function calc() {
  const diff = Math.floor((new Date(props.targetTime) - Date.now()) / 1000)
  remaining.value = Math.max(0, diff)
  if (remaining.value === 0 && !endedEmitted) {
    endedEmitted = true
    clearTimer()
    emit('ended')
  }
}

function schedule() {
  clearTimer()
  calc()
  if (remaining.value > 0) {
    const delay = document.visibilityState === 'visible' ? 1000 : 5000
    timer = window.setTimeout(schedule, delay)
  }
}

function clearTimer() {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

function handleVisibilityChange() {
  schedule()
}

const formatted = computed(() => {
  const s = remaining.value
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  return [h, m, sec].map(v => String(v).padStart(2, '0')).join(':')
})

watch(() => props.targetTime, () => {
  endedEmitted = false
  schedule()
})

onMounted(() => {
  schedule()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})
onUnmounted(() => {
  clearTimer()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.countdown { display: inline-flex; align-items: center; gap: 8px; font-size: 16px; }
.label { color: #909399; }
.time { font-weight: bold; font-size: 20px; font-variant-numeric: tabular-nums; color: #409eff; }
.urgent .time { color: #f56c6c; animation: pulse 1s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
</style>
