<template>
  <div class="page-shell">
    <PageHeader title="数据看板" subtitle="实时传感器数据展示" />

    <BaseCard class="info-banner">
      <span class="info-dot" />
      默认读取真实串口数据。当前数据源：<strong>{{ sourceLabel }}</strong>
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
      <SensorLineChart title="💧 湿度变化" :labels="labels" :values="humidities" color="#0ea5e9" />
      <SensorLineChart title="🔥 燃气浓度变化" :labels="labels" :values="gasValues" color="#8b5cf6" />
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
          <router-link to="/recent">查看最近数据</router-link>
          <router-link to="/analytics">查看统计分析</router-link>
          <router-link to="/forecast">查看温度预测</router-link>
          <router-link to="/agent">询问温度计 Agent</router-link>
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
    REAL_SERIAL: '真实串口数据',
    REAL_MQTT: '真实 MQTT 数据',
    MOCK: '模拟演示数据',
    ALL: '全部数据',
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
  return '#10b981'
})

const metrics = computed(() => {
  if (!hasData.value || !latest.value) {
    return [{ icon: '⏳', label: '状态', value: '等待 Hi3861 真实设备数据。', iconBg: '#f3f4f6', iconColor: '#64748b' }]
  }
  const d = latest.value
  return [
    { icon: '🖥', label: '设备 ID', value: d.deviceId, iconBg: '#eff6ff', iconColor: '#2563eb' },
    { icon: '🌡', label: '温度', value: fmt(d.temperature, 1, '℃'), iconBg: '#fff7ed', iconColor: '#f97316', valueColor: tempColor.value },
    { icon: '💧', label: '湿度', value: fmt(d.humidity, 1, '%'), iconBg: '#f0f9ff', iconColor: '#0ea5e9' },
    { icon: '🔥', label: '燃气浓度', value: fmt(d.gas, 1, 'ppm'), iconBg: '#f5f3ff', iconColor: '#8b5cf6' },
    { icon: '🛡', label: '安全状态', value: d.status, iconBg: d.status === 'WARNING' ? '#fef2f2' : '#ecfdf5', iconColor: d.status === 'WARNING' ? '#ef4444' : '#10b981', valueColor: d.status === 'WARNING' ? '#ef4444' : '#10b981' },
    { icon: '📡', label: '数据来源', value: sourceLabel.value, iconBg: '#f5f3ff', iconColor: '#7c3aed' },
    { icon: '🕐', label: '更新时间', value: fmt(d.createdAt), iconBg: '#f3f4f6', iconColor: '#64748b' },
    { icon: '🔌', label: '设备连接', value: d.dataSource === 'MOCK' ? '模拟演示数据' : 'Hi3861 真实设备', iconBg: '#ecfdf5', iconColor: '#10b981' },
  ]
})

const summaryText = computed(() => {
  if (!hasData.value || !latest.value) return '等待更多真实数据...'
  const d = latest.value
  const safe = d.status === 'SAFE'
  return `当前环境总体${safe ? '安全' : '存在风险'}，温度处于${d.temperature > 35 ? '偏高' : '正常'}范围，湿度${d.humidity > 80 ? '偏高' : '稳定'}，燃气浓度${d.gas > 300 ? '超过风险阈值' : '未超过风险阈值'}。`
})

const changeText = computed(() => {
  if (recent.value.length < 2) return '等待更多真实数据...'
  const first = recent.value[recent.value.length - 1]
  const last = recent.value[0]
  const dt = Number(last.temperature) - Number(first.temperature)
  const dh = Number(last.humidity) - Number(first.humidity)
  const dg = Number(last.gas) - Number(first.gas)
  return `温度${dt >= 0 ? '上升' : '下降'} ${Math.abs(dt).toFixed(1)}℃，湿度${dh >= 0 ? '上升' : '下降'} ${Math.abs(dh).toFixed(1)}%，燃气${dg >= 0 ? '上升' : '下降'} ${Math.abs(dg).toFixed(1)} ppm。`
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
  padding: 12px 16px; margin-bottom: 20px;
  border-left: 4px solid #2563eb; font-size: 14px; color: #1f2937;
}
.info-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #2563eb;
}
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
.insight-card { padding: 20px; }
.insight-card h4 { font-size: 15px; font-weight: 600; color: #0f172a; margin-bottom: 10px; }
.insight-card p { font-size: 13px; color: #64748b; line-height: 1.7; }
.insight-time { font-size: 11px; color: #94a3b8; margin-top: 8px; }
.quick-actions { display: flex; flex-direction: column; gap: 8px; }
.quick-actions a {
  display: block; padding: 8px 12px; border-radius: 8px;
  background: #f8fbff; color: #2563eb; text-decoration: none;
  font-size: 13px; font-weight: 500; transition: all 0.15s;
}
.quick-actions a:hover { background: #eff6ff; }
@media (max-width: 1024px) {
  .insight-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 760px) {
  .insight-grid { grid-template-columns: 1fr; }
}
</style>
