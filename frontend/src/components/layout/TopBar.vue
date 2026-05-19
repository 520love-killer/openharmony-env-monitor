<template>
  <header class="topbar">
    <div class="topbar-left">
      <span class="topbar-icon">🌡</span>
      <span class="topbar-title">温度计 · 环境监测智能助手</span>
    </div>
    <div class="topbar-right">
      <span class="status-indicator">
        <span class="status-dot online"></span>
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
  if (agentStore.agentStatus?.mode === 'deepseek') return 'DeepSeek 模式'
  if (agentStore.agentStatus?.mode === 'mock') return 'Mock 模式'
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
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(148,163,184,0.22);
  flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; gap: 10px; }
.topbar-icon { font-size: 22px; }
.topbar-title { font-size: 15px; font-weight: 600; color: #0f172a; }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.status-indicator {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; font-weight: 500; color: #10b981;
}
.status-dot {
  width: 7px; height: 7px; border-radius: 50%; display: inline-block;
}
.status-dot.online { background: #10b981; box-shadow: 0 0 5px rgba(16,185,129,0.4); }
.topbar-badge {
  font-size: 11px; font-weight: 500;
  padding: 3px 10px; border-radius: 999px;
}
.badge-deepseek { color: #7c3aed; background: #f5f3ff; }
.badge-mock { color: #d97706; background: #fffbeb; }
.badge-offline { color: #64748b; background: #f3f4f6; }
.topbar-link {
  font-size: 12px; color: #64748b; text-decoration: none; cursor: pointer; transition: color 0.15s;
}
.topbar-link:hover { color: #2563eb; }
.topbar-user {
  font-size: 16px; cursor: pointer; width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #f3f4f6;
}
</style>
