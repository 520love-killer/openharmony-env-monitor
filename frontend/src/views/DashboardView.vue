<template>
  <div class="page-shell">
    <PageHeader title="数据看板" subtitle="实时传感器数据 · 环境监测大屏" />

    <BaseCard class="info-banner">
      <span class="info-dot" />
      数据通道：<strong>{{ sourceLabel }}</strong> · Hi3861 串口直连
    </BaseCard>

    <div class="metric-grid">
      <MetricCard
        v-for="m in metrics"
        :key="m.label"
        :icon="m.icon"
        :label="m.label"
        :value="m.value"
        :icon-bg="m.iconBg"
        :icon-color="m.iconColor"
        :value-color="m.valueColor"
      />
    </div>

    <SectionHeader title="趋势图" />
    <div class="chart-grid">
      <SensorLineChart title="🌡 温度变化" :labels="labels" :values="temps" color="#f97316" />
      <SensorLineChart title="💧 湿度变化" :labels="labels" :values="humidities" color="#38bdf8" />
      <SensorLineChart title="🔥 燃气浓度变化" :labels="labels" :values="gasValues" color="#a78bfa" />
    </div>

    <SectionHeader title="智能环境摘要" />
    <div class="insight-grid">
      <BaseCard class="insight-card">
        <h4>📋 当前环境摘要</h4>
        <p>{{ summaryText }}</p>
      </BaseCard>
      <BaseCard class="insight-card">
        <h4>📈 最近数据变化</h4>
        <p>{{ changeText }}</p>
        <div class="insight-time">{{ lastUpdate }}</div>
      </BaseCard>
      <BaseCard class="insight-card">
        <h4>🚀 快速操作</h4>
        <div class="quick-actions">
          <router-link to="/recent">→ 查看最近数据</router-link>
          <router-link to="/analytics">→ 查看统计分析</router-link>
          <router-link to="/forecast">→ 查看温度预测</router-link>
          <router-link to="/agent">→ 询问温度计 Agent</router-link>
        </div>
      </BaseCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import SensorLineChart from '@/components/charts/SensorLineChart.vue'
import { useAgentStore } from '@/stores/agent'
import { sensorApi } from '@/api/sensor'
import type { SensorData } from '@/types'

const agentStore = useAgentStore()
const source = computed(() => agentStore.source)
const sourceLabel = computed(() => {
  const map: Record<string, string> = {
    REAL_SERIAL: 'REAL_SERIAL',
    REAL_MQTT: 'REAL_MQTT',
    MOCK: 'MOCK',
    ALL: 'ALL',
  }
  return map[source.value] || source.value
})

const latest = ref<SensorData | null>(null)
const recent = ref<SensorData[]>([])
const hasData = ref(false)

const labels = computed(() => [...recent.value].reverse().map(r => fmtTime(r.createdAt)))
const temps = computed(() => [...recent.value].reverse().map(r => num(r.temperature)))
const humidities = computed(() => [...recent.value].reverse().map(r => num(r.humidity)))
const gasValues = computed(() => [...recent.value].reverse().map(r => num(r.gas)))

const tempColor = computed(() => {
  const t = Number(latest.value?.temperature)
  if (t > 35) return '#ef4444'
  if (t > 28) return '#f59e0b'
  return '#22c55e'
})

const metrics = computed(() => {
  if (!hasData.value || !latest.value) {
    return [{ icon: '⏳', label: '状态', value: '等待 Hi3861 设备数据...', iconBg: 'rgba(148,163,184,0.10)', iconColor: '#94a3b8' }]
  }
  const d = latest.value
  return [
    { icon: '🖥', label: '设备 ID', value: d.deviceId, iconBg: 'rgba(56,189,248,0.12)', iconColor: '#38bdf8' },
    { icon: '🌡', label: '温度', value: fmt(d.temperature, 1, '℃'), iconBg: 'rgba(249,115,22,0.12)', iconColor: '#f97316', valueColor: tempColor.value },
    { icon: '💧', label: '湿度', value: fmt(d.humidity, 1, '%'), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '🔥', label: '燃气浓度', value: fmt(d.gas, 1, 'ppm'), iconBg: 'rgba(139,92,246,0.12)', iconColor: '#a78bfa' },
    { icon: '🛡', label: '安全状态', value: d.status, iconBg: d.status === 'WARNING' ? 'rgba(239,68,68,0.14)' : 'rgba(34,197,94,0.12)', iconColor: d.status === 'WARNING' ? '#ef4444' : '#22c55e', valueColor: d.status === 'WARNING' ? '#ef4444' : '#22c55e' },
    { icon: '📡', label: '数据来源', value: sourceLabel.value, iconBg: 'rgba(139,92,246,0.12)', iconColor: '#a78bfa' },
    { icon: '🕐', label: '更新时间', value: fmt(d.createdAt), iconBg: 'rgba(148,163,184,0.10)', iconColor: '#94a3b8' },
    { icon: '🔌', label: '设备连接', value: d.dataSource === 'MOCK' ? '模拟数据' : 'Hi3861 真实设备', iconBg: 'rgba(34,197,94,0.12)', iconColor: '#22c55e' },
  ]
})

const summaryText = computed(() => {
  if (!hasData.value || !latest.value) return '等待更多真实数据...'
  const d = latest.value
  const safe = d.status === 'SAFE'
  return `当前环境总体${safe ? '安全' : '存在风险'}，温度处于${Number(d.temperature) > 35 ? '偏高' : '正常'}范围，湿度${Number(d.humidity) > 80 ? '偏高' : '稳定'}，燃气浓度${Number(d.gas) > 300 ? '超过风险阈值' : '未超过风险阈值'}。`
})

const changeText = computed(() => {
  if (recent.value.length < 2) return '等待更多真实数据...'
  const first = recent.value[recent.value.length - 1]
  const last = recent.value[0]
  const dt = Number(last.temperature) - Number(first.temperature)
  const dh = Number(last.humidity) - Number(first.humidity)
  const dg = Number(last.gas) - Number(first.gas)
  return `温度${dt >= 0 ? '↑' : '↓'} ${Math.abs(dt).toFixed(1)}℃，湿度${dh >= 0 ? '↑' : '↓'} ${Math.abs(dh).toFixed(1)}%，燃气${dg >= 0 ? '↑' : '↓'} ${Math.abs(dg).toFixed(1)} ppm。`
})

const lastUpdate = computed(() => {
  if (!latest.value) return ''
  return '更新时间：' + fmt(latest.value.createdAt)
})

async function load() {
  try {
    const [lRes, rRes] = await Promise.all([
      sensorApi.latest(source.value),
      sensorApi.recent(source.value, 50),
    ])
    hasData.value = lRes.data?.hasData === true && !!lRes.data?.data
    latest.value = lRes.data?.data || null
    recent.value = Array.isArray(rRes.data) ? rRes.data : []
  } catch { /* ignore */ }
}

watch(source, load)
onMounted(load)

function fmt(v: any, d?: number, u?: string) {
  if (v === null || v === undefined || v === '') return '-'
  if (typeof v === 'string' && v.match(/^\d{4}-\d{2}-\d{2}/)) return new Date(v).toLocaleString()
  const n = Number(v)
  if (!Number.isFinite(n)) return String(v)
  return `${n.toFixed(d ?? 1)}${u ? ' ' + u : ''}`
}
function fmtTime(v: string) {
  const d = new Date(v)
  return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
function num(v: any) {
  const n = Number(v)
  return Number.isFinite(n) ? n : null
}
</script>

<style scoped>
.info-banner {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 18px; margin-bottom: 20px;
  border-left: 4px solid var(--blue);
  font-size: 13px; color: var(--text-muted);
  background: linear-gradient(90deg, rgba(56,189,248,0.06), transparent);
}
.info-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--blue);
  box-shadow: 0 0 8px var(--blue-glow);
  animation: breath 2s ease-in-out infinite;
}
@keyframes breath {
  0%, 100% { opacity: 1; box-shadow: 0 0 8px rgba(56,189,248,0.4); }
  50% { opacity: 0.5; box-shadow: 0 0 16px rgba(56,189,248,0.8); }
}
.info-banner strong { color: var(--text-primary); }

.metric-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  margin-bottom: 24px;
}
.chart-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  margin-bottom: 24px;
}
.insight-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.insight-card { padding: 22px; }
.insight-card h4 { font-size: 15px; font-weight: 600; color: var(--text-heading); margin-bottom: 12px; }
.insight-card p { font-size: 13px; color: var(--text-muted); line-height: 1.7; }
.insight-time { font-size: 11px; color: var(--text-dim); margin-top: 10px; }
.quick-actions { display: flex; flex-direction: column; gap: 8px; }
.quick-actions a {
  display: block; padding: 10px 14px; border-radius: 10px;
  background: rgba(56, 189, 248, 0.06);
  color: var(--blue); text-decoration: none;
  font-size: 13px; font-weight: 500; transition: all 0.2s;
  border: 1px solid transparent;
}
.quick-actions a:hover {
  background: rgba(56, 189, 248, 0.10);
  border-color: rgba(56, 189, 248, 0.20);
}
@media (max-width: 1024px) {
  .insight-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 760px) {
  .insight-grid { grid-template-columns: 1fr; }
}
</style>
