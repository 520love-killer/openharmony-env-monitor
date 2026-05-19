<template>
  <header class="topbar">
    <div class="topbar-left">
      <span class="topbar-icon">🌡</span>
      <span class="topbar-title">温度计 · 环境监测智能助手</span>
    </div>
    <div class="topbar-right">
      <span class="status-indicator">
        <span class="breath-dot" style="color: #22c55e;" />
        在线
      </span>
      <span class="topbar-badge" :class="modeClass">{{ modeLabel }}</span>
      <router-link to="/about" class="topbar-link">帮助文档</router-link>
      <span class="topbar-user">👤</span>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAgentStore } from '@/stores/agent'

const agentStore = useAgentStore()

const modeLabel = computed(() => {
  if (agentStore.agentStatus?.mode === 'deepseek') return 'DeepSeek'
  if (agentStore.agentStatus?.mode === 'mock') return 'Mock'
  return '离线'
})

const modeClass = computed(() => {
  if (agentStore.agentStatus?.mode === 'deepseek') return 'badge-deepseek'
  if (agentStore.agentStatus?.mode === 'mock') return 'badge-mock'
  return 'badge-offline'
})
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 28px;
  background: rgba(2, 6, 23, 0.70);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(56, 189, 248, 0.12);
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}
.topbar::after {
  content: '';
  position: absolute;
  bottom: -1px; left: 5%; right: 5%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(56,189,248,0.15), transparent);
}
.topbar-left { display: flex; align-items: center; gap: 10px; }
.topbar-icon { font-size: 22px; }
.topbar-title {
  font-size: 15px; font-weight: 600; color: var(--text-heading);
  letter-spacing: 0.3px;
}
.topbar-right { display: flex; align-items: center; gap: 14px; }
.status-indicator {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px; font-weight: 500; color: var(--green);
}
.topbar-badge {
  font-size: 11px; font-weight: 600;
  padding: 4px 12px; border-radius: 999px;
  letter-spacing: 0.3px;
}
.badge-deepseek {
  color: #c4b5fd; background: rgba(139, 92, 246, 0.18);
  border: 1px solid rgba(139, 92, 246, 0.30);
  box-shadow: 0 0 12px rgba(139, 92, 246, 0.12);
}
.badge-mock {
  color: #fcd34d; background: rgba(245, 158, 11, 0.15);
  border: 1px solid rgba(245, 158, 11, 0.25);
}
.badge-offline {
  color: var(--text-dim); background: rgba(100, 116, 139, 0.12);
  border: 1px solid rgba(100, 116, 139, 0.18);
}
.topbar-link {
  font-size: 12px; color: var(--text-muted); text-decoration: none; cursor: pointer; transition: color 0.2s;
}
.topbar-link:hover { color: var(--blue); }
.topbar-user {
  font-size: 16px; cursor: pointer; width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.12);
  transition: background 0.2s;
}
.topbar-user:hover { background: rgba(148, 163, 184, 0.22); }
</style>
