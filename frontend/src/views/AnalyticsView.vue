<template>
  <div class="page-shell">
    <PageHeader title="统计分析" subtitle="基于最近 50 条真实数据计算统计指标" />

    <BaseCard class="summary-card">
      <div class="summary-head">
        <span class="summary-icon">◈</span>
        <strong>分析结论</strong>
      </div>
      <p>{{ summaryText }}</p>
    </BaseCard>

    <div class="stat-grid">
      <div v-for="m in metrics" :key="m.label" class="stat-item">
        <div class="stat-icon" :style="{ background: m.iconBg, color: m.iconColor }">{{ m.icon }}</div>
        <span class="stat-label">{{ m.label }}</span>
        <strong class="stat-value">{{ m.value }}</strong>
        <div class="stat-line" />
      </div>
    </div>

    <BaseCard v-if="trendText" class="trend-card">
      <div class="trend-head">
        <span class="trend-dot" />
        <strong>趋势解释</strong>
      </div>
      <div class="trend-row">
        <span>温度趋势：<strong :class="trendColor(trend?.temperatureTrend)">{{ trend?.temperatureTrend }}</strong></span>
        <span>湿度趋势：<strong :class="trendColor(trend?.humidityTrend)">{{ trend?.humidityTrend }}</strong></span>
        <span>燃气趋势：<strong :class="trendColor(trend?.gasTrend)">{{ trend?.gasTrend }}</strong></span>
      </div>
      <p>{{ trend?.explanation }}</p>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import { useAgentStore } from '@/stores/agent'
import { analyticsApi } from '@/api/analytics'
import type { AnalyticsSummary, TrendResponse } from '@/types'

const agentStore = useAgentStore()
const source = computed(() => agentStore.source)

const summary = ref<AnalyticsSummary | null>(null)
const trend = ref<TrendResponse | null>(null)

const summaryText = computed(() => {
  if (!summary.value?.success) return summary.value?.message || '等待统计分析结果...'
  const s = summary.value
  return `基于最近 ${s.sampleCount} 条数据，整体趋势 ${s.trend}，波动程度 ${s.volatilityLevel}。数据来源：${source.value}。`
})

const metrics = computed(() => {
  if (!summary.value?.success) return [{ icon: '⏳', label: '状态', value: '数据不足', iconBg: 'rgba(148,163,184,0.10)', iconColor: '#94a3b8' }]
  const s = summary.value
  return [
    { icon: '📊', label: '样本数', value: String(s.sampleCount), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '🌡', label: '平均温度', value: fmt(s.temperatureAvg, 2, '℃'), iconBg: 'rgba(249,115,22,0.10)', iconColor: '#f97316' },
    { icon: '🔥', label: '最高温度', value: fmt(s.temperatureMax, 2, '℃'), iconBg: 'rgba(239,68,68,0.10)', iconColor: '#ef4444' },
    { icon: '❄', label: '最低温度', value: fmt(s.temperatureMin, 2, '℃'), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '📐', label: '温度标准差', value: fmt(s.temperatureStd, 2), iconBg: 'rgba(139,92,246,0.10)', iconColor: '#a78bfa' },
    { icon: '💧', label: '平均湿度', value: fmt(s.humidityAvg, 2, '%'), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '🔥', label: '燃气最大值', value: fmt(s.gasMax, 2, 'ppm'), iconBg: 'rgba(139,92,246,0.10)', iconColor: '#a78bfa' },
    { icon: '📈', label: '波动程度', value: s.volatilityLevel, iconBg: 'rgba(245,158,11,0.10)', iconColor: '#f59e0b' },
    { icon: '🌡', label: '温度变化率', value: fmt(s.temperatureChangeRate, 3, '℃/min'), iconBg: 'rgba(249,115,22,0.10)', iconColor: '#f97316' },
    { icon: '💧', label: '湿度变化率', value: fmt(s.humidityChangeRate, 3, '%/min'), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '🔥', label: '燃气变化率', value: fmt(s.gasChangeRate, 3, 'ppm/min'), iconBg: 'rgba(139,92,246,0.10)', iconColor: '#a78bfa' },
    { icon: '📉', label: '整体趋势', value: s.trend, iconBg: 'rgba(34,197,94,0.10)', iconColor: '#22c55e' },
  ]
})

const trendText = computed(() => trend.value?.success)

function trendColor(v?: string) {
  if (!v) return ''
  if (v.includes('升') || v.includes('UP')) return 'up'
  if (v.includes('降') || v.includes('DOWN')) return 'down'
  return 'stable'
}

async function load() {
  try {
    const [sRes, tRes] = await Promise.all([
      analyticsApi.summary(source.value),
      analyticsApi.trend(source.value),
    ])
    summary.value = sRes.data
    trend.value = tRes.data
  } catch { /* ignore */ }
}

watch(source, load)
onMounted(load)

function fmt(v: any, d?: number, u?: string) {
  if (v === null || v === undefined || v === '') return '-'
  const n = Number(v)
  if (!Number.isFinite(n)) return String(v)
  return `${n.toFixed(d ?? 1)}${u ? ' ' + u : ''}`
}
</script>

<style scoped>
.summary-card { padding: 18px 22px; margin-bottom: 20px; }
.summary-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.summary-icon { color: var(--blue); font-size: 14px; }
.summary-card strong { font-size: 14px; color: var(--text-heading); }
.summary-card p { font-size: 13px; color: var(--text-muted); line-height: 1.6; }

.stat-grid { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); margin-bottom: 20px; }
.stat-item {
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.72), rgba(15, 23, 42, 0.62));
  backdrop-filter: blur(14px);
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 18px;
  padding: 16px;
  display: flex; flex-direction: column; gap: 6px;
  position: relative; overflow: hidden;
  transition: all 0.2s;
}
.stat-item:hover { border-color: rgba(56,189,248,0.25); transform: translateY(-1px); }
.stat-icon { width: 34px; height: 34px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.stat-label { font-size: 11px; color: var(--text-dim); font-weight: 500; }
.stat-value { font-size: 22px; font-weight: 800; color: var(--text-heading); line-height: 1.2; }
.stat-line {
  position: absolute; bottom: 0; left: 10%; right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(56,189,248,0.15), transparent);
}

.trend-card { padding: 18px 22px; }
.trend-head { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.trend-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--blue);
  box-shadow: 0 0 8px var(--blue-glow);
}
.trend-card strong { font-size: 14px; color: var(--text-heading); }
.trend-row { display: flex; flex-wrap: wrap; gap: 18px; margin-bottom: 10px; font-size: 13px; color: var(--text-muted); }
.trend-row strong { font-size: 13px; }
.up { color: #ef4444; text-shadow: 0 0 8px rgba(239,68,68,0.3); }
.down { color: #38bdf8; text-shadow: 0 0 8px rgba(56,189,248,0.3); }
.stable { color: #22c55e; text-shadow: 0 0 8px rgba(34,197,94,0.3); }
.trend-card p { font-size: 13px; color: var(--text-muted); line-height: 1.6; }
</style>
