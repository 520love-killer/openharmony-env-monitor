<template>
  <BaseCard class="metric-card">
    <div class="metric-top">
      <div class="metric-icon" :style="{ background: iconBg, color: iconColor }">
        {{ icon }}
      </div>
      <div class="metric-glow-line" />
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
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 120px;
}
.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.metric-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.20);
}
.metric-glow-line {
  height: 1px;
  width: 40px;
  background: linear-gradient(90deg, rgba(56,189,248,0.25), transparent);
  border-radius: 1px;
}
.metric-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  letter-spacing: 0.3px;
}
.metric-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-heading);
  line-height: 1.2;
  word-break: break-word;
  letter-spacing: -0.3px;
}
</style>
