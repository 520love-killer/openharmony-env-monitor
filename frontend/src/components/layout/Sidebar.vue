<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <svg viewBox="0 0 48 64" width="42" height="56">
          <defs>
            <linearGradient id="thermo-grad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#38bdf8"/>
              <stop offset="55%" stop-color="#ef4444"/>
              <stop offset="100%" stop-color="#f59e0b"/>
            </linearGradient>
          </defs>
          <rect x="16" y="6" width="16" height="42" rx="8" fill="url(#thermo-grad)"/>
          <circle cx="24" cy="12" r="8" fill="#38bdf8"/>
          <circle cx="24" cy="50" r="10" fill="#f59e0b"/>
          <circle cx="20" cy="24" r="2" fill="#fff" opacity="0.9"/>
          <circle cx="28" cy="24" r="2" fill="#fff" opacity="0.9"/>
          <path d="M21 30 Q24 33 27 30" stroke="#fff" stroke-width="1.5" fill="none" stroke-linecap="round" opacity="0.9"/>
        </svg>
      </div>
      <div>
        <h2 class="sidebar-title">温度计</h2>
        <p class="sidebar-subtitle">AIoT 环境监测</p>
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
        <span class="nav-label">{{ item.label }}</span>
        <span v-if="route.path === item.path" class="nav-glow" />
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="source-control">
        <label class="source-label">数据源</label>
        <select v-model="source" @change="onSourceChange">
          <option value="REAL_SERIAL">REAL_SERIAL</option>
          <option value="REAL_MQTT">REAL_MQTT</option>
          <option value="MOCK">MOCK</option>
          <option value="ALL">ALL</option>
        </select>
      </div>
      <div class="terminal-block">
        <div class="term-line">
          <span class="term-dot term-dot-green" />
          <span>数据源: REAL_SERIAL</span>
        </div>
        <div class="term-line">
          <span class="term-dot" :class="agentStatus?.mode === 'deepseek' ? 'term-dot-green' : 'term-dot-amber'" />
          <span>Agent: {{ agentStatus?.mode === 'deepseek' ? 'DeepSeek' : agentStatus?.mode === 'mock' ? 'Mock' : '离线' }}</span>
        </div>
        <div class="term-line">
          <span class="term-label">模型:</span>
          <span>{{ agentStatus?.model || '-' }}</span>
        </div>
      </div>
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

const agentStatus = computed(() => agentStore.agentStatus)

function onSourceChange() {
  agentStore.refreshCurrentPage()
}

const menu = [
  { path: '/agent', label: 'Agent 驾驶舱', icon: '🤖' },
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
  background: linear-gradient(180deg, #020617 0%, #0b1424 40%, #0f172a 100%);
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  border-right: 1px solid rgba(148, 163, 184, 0.10);
  position: relative;
}
.sidebar::after {
  content: '';
  position: absolute;
  top: 0; right: 0; bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(56,189,248,0.20), transparent);
  pointer-events: none;
}

.sidebar-header {
  padding: 22px 18px 18px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
  gap: 12px;
}
.sidebar-logo { flex-shrink: 0; filter: drop-shadow(0 0 8px rgba(56,189,248,0.3)); }
.sidebar-title { font-size: 18px; color: var(--text-heading); font-weight: 700; letter-spacing: 0.5px; }
.sidebar-subtitle { font-size: 11px; color: var(--text-dim); margin-top: 2px; letter-spacing: 0.5px; }

.sidebar-nav { flex: 1; padding: 12px 8px; }
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
  border-radius: 8px;
  margin: 1px 0;
  position: relative;
  overflow: hidden;
}
.nav-item:hover {
  background: rgba(56, 189, 248, 0.06);
  color: var(--text-primary);
}
.nav-item.active {
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.28), rgba(139, 92, 246, 0.14));
  color: #e5f0ff;
  border-left: 3px solid var(--blue);
  box-shadow: inset 0 0 24px rgba(56, 189, 248, 0.06);
  font-weight: 600;
}
.nav-glow {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  border-radius: 0 3px 3px 0;
  background: var(--blue);
  box-shadow: 0 0 12px var(--blue-glow), 0 0 24px rgba(56, 189, 248, 0.15);
}
.nav-icon { font-size: 16px; width: 22px; text-align: center; flex-shrink: 0; }
.nav-label { white-space: nowrap; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.source-control { margin-bottom: 12px; }
.source-label { display: block; font-size: 10px; color: var(--text-dim); margin-bottom: 5px; text-transform: uppercase; letter-spacing: 1px; }
.source-control select {
  width: 100%; height: 32px;
  border: 1px solid rgba(255,255,255,0.10);
  border-radius: 6px;
  background: rgba(255,255,255,0.05);
  color: var(--text-muted);
  font-size: 11px;
  padding: 0 8px;
  cursor: pointer;
  outline: none;
  font-family: "Cascadia Code", "Fira Code", monospace;
}
.source-control select:focus {
  border-color: rgba(56,189,248,0.35);
}

.terminal-block {
  background: rgba(2, 6, 23, 0.60);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 8px;
  padding: 10px 12px;
  font-family: "Cascadia Code", "Fira Code", monospace;
  font-size: 10px;
  line-height: 1.7;
}
.term-line { display: flex; align-items: center; gap: 6px; color: var(--text-dim); }
.term-dot {
  width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0;
  background: var(--text-dim);
}
.term-dot-green { background: var(--green); box-shadow: 0 0 6px rgba(34, 197, 94, 0.5); }
.term-dot-amber { background: var(--amber); box-shadow: 0 0 6px rgba(245, 158, 11, 0.4); }
.term-label { color: var(--text-dim); }
</style>
