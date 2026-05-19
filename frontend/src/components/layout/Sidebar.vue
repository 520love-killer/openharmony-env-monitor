<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">🌡</div>
      <div>
        <h2 class="sidebar-title">温度计</h2>
        <p class="sidebar-subtitle">环境监测智能助手</p>
      </div>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in menu"
        :key="item.path"
        :to="item.path"
        :class="['nav-item', { active: route.path === item.path }]"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="source-control">
        <label>数据源</label>
        <select v-model="source" @change="onSourceChange">
          <option value="REAL_SERIAL">真实串口数据</option>
          <option value="REAL_MQTT">真实 MQTT 数据</option>
          <option value="MOCK">模拟演示数据</option>
          <option value="ALL">全部数据</option>
        </select>
      </div>
      <div class="sidebar-status">{{ statusText }}</div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAgentStore } from '@/stores/agent'

const route = useRoute()
const agentStore = useAgentStore()

const source = computed({
  get: () => agentStore.source,
  set: (v) => agentStore.setSource(v),
})

const statusText = computed(() => {
  if (agentStore.agentStatus?.mode === 'deepseek') {
    return `Agent: 🟢 DeepSeek | 模型: ${agentStore.agentStatus.model || '-'}`
  }
  if (agentStore.agentStatus?.mode === 'mock') {
    return `Agent: 🟡 Mock | 模型: ${agentStore.agentStatus.model || '-'}`
  }
  return '等待数据...'
})

function onSourceChange() {
  agentStore.refreshCurrentPage()
}

const menu = [
  { path: '/agent', label: '温度计 Agent', icon: '🤖' },
  { path: '/dashboard', label: '数据看板', icon: '📊' },
  { path: '/analytics', label: '统计分析', icon: '📈' },
  { path: '/forecast', label: '温度预测', icon: '🔮' },
  { path: '/anomaly', label: '异常检测', icon: '⚠' },
  { path: '/recent', label: '最近数据', icon: '📋' },
  { path: '/system', label: '系统状态', icon: '⚙' },
  { path: '/about', label: '关于项目', icon: '📖' },
]
</script>

<style scoped>
.sidebar {
  width: 240px;
  min-width: 240px;
  height: 100vh;
  flex-shrink: 0;
  background: linear-gradient(180deg, #0f172a 0%, #1a2332 100%);
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.sidebar-header {
  padding: 22px 18px 18px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
  gap: 10px;
}
.sidebar-logo { font-size: 28px; line-height: 1; }
.sidebar-title { font-size: 17px; color: #f1f5f9; font-weight: 600; }
.sidebar-subtitle { font-size: 11px; color: #64748b; margin-top: 2px; }

.sidebar-nav { flex: 1; padding: 10px 0; }
.nav-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 18px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  border-left: 3px solid transparent;
  margin: 0 8px;
  border-radius: 8px;
}
.nav-item:hover { background: rgba(255,255,255,0.04); color: #e2e8f0; }
.nav-item.active {
  background: rgba(37,99,235,0.12);
  color: #93c5fd;
  border-left-color: transparent;
}
.nav-icon { font-size: 15px; width: 20px; text-align: center; }

.sidebar-footer {
  padding: 14px 16px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.source-control { margin-bottom: 8px; }
.source-control label { display: block; font-size: 11px; color: #64748b; margin-bottom: 4px; }
.source-control select {
  width: 100%; height: 30px; border: 1px solid rgba(255,255,255,0.08); border-radius: 6px;
  background: rgba(255,255,255,0.06); color: #cbd5e1; font-size: 11px; padding: 0 8px;
  cursor: pointer;
}
.sidebar-status { font-size: 11px; color: #64748b; line-height: 1.4; }
</style>
