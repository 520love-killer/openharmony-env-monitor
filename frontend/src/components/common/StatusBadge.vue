<template>
  <span class="badge" :class="typeClass">
    <span v-if="dot" class="dot" />
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  type?: 'safe' | 'warning' | 'deepseek' | 'mock' | 'serial' | 'info' | 'primary'
  dot?: boolean
}>()

const typeClass = computed(() => {
  return props.type ? `badge-${props.type}` : ''
})
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 26px;
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}
.dot {
  width: 7px; height: 7px; border-radius: 50%;
}
.badge-safe { color: #059669; background: #ecfdf5; }
.badge-safe .dot { background: #10b981; box-shadow: 0 0 5px rgba(16,185,129,0.4); }
.badge-warning { color: #dc2626; background: #fef2f2; }
.badge-warning .dot { background: #ef4444; }
.badge-deepseek { color: #7c3aed; background: #f5f3ff; }
.badge-mock { color: #d97706; background: #fffbeb; }
.badge-serial { color: #059669; background: #ecfdf5; }
.badge-info { color: #2563eb; background: #eff6ff; }
.badge-primary { color: #2563eb; background: #eff6ff; }
</style>
