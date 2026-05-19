<template>
  <div class="page-shell">
    <div class="agent-grid">
      <div class="agent-main">
        <AgentHero :source="source" :model="agentStatus?.model || 'deepseek-chat'" />
        <AgentChat :source="source" />
      </div>
      <div class="agent-side">
        <ToolPanel @select="onQuick" />
        <QuickPrompts @select="onQuick" />
        <RealtimeOverview
          :temp="rt.temp"
          :humidity="rt.humidity"
          :gas="rt.gas"
          :status="rt.status"
          :status-class="rt.statusClass"
          :footer="rt.footer"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import AgentHero from '@/components/agent/AgentHero.vue'
import AgentChat from '@/components/agent/AgentChat.vue'
import ToolPanel from '@/components/agent/ToolPanel.vue'
import QuickPrompts from '@/components/agent/QuickPrompts.vue'
import RealtimeOverview from '@/components/agent/RealtimeOverview.vue'
import { useAgentStore } from '@/stores/agent'
import { sensorApi } from '@/api/sensor'

const agentStore = useAgentStore()
const source = computed(() => agentStore.source)
const agentStatus = computed(() => agentStore.agentStatus)

const rt = ref({ temp: '-- ℃', humidity: '-- %', gas: '-- ppm', status: '等待数据', statusClass: '', footer: '等待真实设备数据...' })

async function loadRealtime() {
  try {
    const res = await sensorApi.latest(source.value)
    const payload = res.data
    if (payload?.hasData && payload.data) {
      const d = payload.data
      rt.value = {
        temp: fmt(d.temperature, 2, '℃'),
        humidity: fmt(d.humidity, 1, '%'),
        gas: fmt(d.gas, 1, 'ppm'),
        status: d.status || '-',
        statusClass: d.status === 'WARNING' ? 'warning-text' : 'safe-text',
        footer: `数据来源：${d.dataSource || source.value}（Hi3861）`,
      }
    } else {
      rt.value = {
        temp: '-- ℃', humidity: '-- %', gas: '-- ppm',
        status: '等待数据', statusClass: '',
        footer: '等待真实设备数据...',
      }
    }
  } catch { /* ignore */ }
}

function onQuick(q: string) {
  window.dispatchEvent(new CustomEvent('agent-quick-prompt', { detail: q }))
}

function fmt(v: any, d?: number, u?: string) {
  const n = Number(v)
  if (!Number.isFinite(n)) return '--'
  return `${n.toFixed(d ?? 1)}${u ? ' ' + u : ''}`
}

let timer: any
onMounted(() => {
  agentStore.checkStatus()
  loadRealtime()
  timer = setInterval(loadRealtime, 5000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.agent-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
  align-items: start;
}
.agent-main { display: flex; flex-direction: column; gap: 20px; min-width: 0; }
.agent-side { display: flex; flex-direction: column; gap: 20px; width: 360px; min-width: 0; }
@media (max-width: 1100px) {
  .agent-grid { grid-template-columns: 1fr; }
  .agent-side { width: 100%; }
}
</style>
