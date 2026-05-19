<template>
  <BaseCard class="metric-card">
    <div class="metric-top">
      <div class="metric-icon" :style="{ background: iconBg, color: iconColor }">
        {{ icon }}
      </div>
    </div>
    <span class="metric-label">{{ label }}</span>
    <strong class="metric-value" :style="valueStyle">{{ value }}</strong>
  </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from './BaseCard.vue'
import { computed } from 'vue'

const props = defineProps<{
  icon: string
  label: string
  value: string | number
  iconBg?: string
  iconColor?: string
  valueColor?: string
}>()

const valueStyle = computed(() => {
  if (props.valueColor) return { color: props.valueColor }
  return {}
})
</script>

<style scoped>
.metric-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 110px;
}
.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.metric-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.metric-label {
  font-size: 13px;
  color: #64748b;
}
.metric-value {
  font-size: 21px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.35;
  word-break: break-word;
}
</style>
