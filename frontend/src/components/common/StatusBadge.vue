<template>
  <span class="badge" :class="typeClass">
    <span v-if="dot" class="badge-dot" :class="dotClass" />
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  type?: 'safe' | 'warning' | 'danger' | 'deepseek' | 'mock' | 'serial' | 'info' | 'primary'
  dot?: boolean
}>()

const typeClass = computed(() => props.type ? `badge-${props.type}` : '')
const dotClass = computed(() => {
  if (props.type === 'safe') return 'dot-safe'
  if (props.type === 'warning' || props.type === 'danger') return 'dot-danger'
  return 'dot-info'
})
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 26px;
  border-radius: 999px;
  padding: 3px 12px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  letter-spacing: 0.3px;
}
.badge-dot {
  width: 7px; height: 7px; border-radius: 50%;
}
.dot-safe { background: var(--green); box-shadow: 0 0 6px rgba(34, 197, 94, 0.5); animation: breath 2s ease-in-out infinite; }
.dot-danger { background: var(--red); box-shadow: 0 0 6px rgba(239, 68, 68, 0.4); }
.dot-info { background: var(--blue); box-shadow: 0 0 6px rgba(56, 189, 248, 0.4); }

@keyframes breath {
  0%, 100% { opacity: 1; box-shadow: 0 0 6px rgba(34, 197, 94, 0.5); }
  50% { opacity: 0.5; box-shadow: 0 0 14px rgba(34, 197, 94, 0.8); }
}

.badge-safe { color: #4ade80; background: rgba(34, 197, 94, 0.12); border: 1px solid rgba(34, 197, 94, 0.22); }
.badge-warning { color: #fca5a5; background: rgba(239, 68, 68, 0.14); border: 1px solid rgba(239, 68, 68, 0.25); }
.badge-danger { color: #fca5a5; background: rgba(239, 68, 68, 0.18); border: 1px solid rgba(239, 68, 68, 0.35); box-shadow: 0 0 12px rgba(239, 68, 68, 0.12); }
.badge-deepseek { color: #c4b5fd; background: rgba(139, 92, 246, 0.15); border: 1px solid rgba(139, 92, 246, 0.28); }
.badge-mock { color: #fcd34d; background: rgba(245, 158, 11, 0.12); border: 1px solid rgba(245, 158, 11, 0.22); }
.badge-serial { color: #67e8f9; background: rgba(56, 189, 248, 0.12); border: 1px solid rgba(56, 189, 248, 0.22); }
.badge-info { color: #93c5fd; background: rgba(59, 130, 246, 0.12); border: 1px solid rgba(59, 130, 246, 0.20); }
.badge-primary { color: #93c5fd; background: rgba(59, 130, 246, 0.14); border: 1px solid rgba(59, 130, 246, 0.25); }
</style>
