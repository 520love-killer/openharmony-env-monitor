<template>
  <div class="page-shell">
    <PageHeader title="统计分析" subtitle="基于最近 50 条数据计算统计指标" />

    <BaseCard class="summary-card">
      <strong>分析结论：</strong>
      {{ summaryText }}
    </BaseCard>

    <div class="stat-grid">
      <MetricCard
        v-for="m in metrics"
        :key="m.label"
        :icon="m.icon"
        :label="m.label"
        :value="m.value"
        :icon-bg="m.iconBg"
        :icon-color="m.iconColor"
      />
    </div>

    <BaseCard v-if="trendText" class="trend-card">
      <div class="trend-row">
        <span>温度趋势：<strong :class="trendColor(trend?.temperatureTrend)">{{ trend?.temperatureTrend }}</strong></span>
        <span>湿度趋势：<strong :class="trendColor(trend?.humidityTrend)">{{ trend?.humidityTrend }}</strong></span>
        <span>燃气趋势：<strong :class="trendColor(trend?.gasTrend)">{{ trend?.gasTrend }}</strong></span>
      </div>
      <p>综合解释：<strong>{{ trend?.explanation }}</strong></p>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import MetricCard from '@/components/common/MetricCard.vue'
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
  if (!summary.value?.success) return [{ icon: '⏳', label: '状态', value: '数据不足', iconBg: '#f3f4f6', iconColor: '#64748b' }]
  const s = summary.value
  return [
    { icon: '📊', label: '样本数', value: String(s.sampleCount), iconBg: '#eff6ff', iconColor: '#2563eb' },
    { icon: '🌡', label: '平均温度', value: fmt(s.temperatureAvg, 2, '℃'), iconBg: '#fff7ed', iconColor: '#f97316' },
    { icon: '🔥', label: '最高温度', value: fmt(s.temperatureMax, 2, '℃'), iconBg: '#fef2f2', iconColor: '#ef4444' },
    { icon: '❄', label: '最低温度', value: fmt(s.temperatureMin, 2, '℃'), iconBg: '#f0f9ff', iconColor: '#0ea5e9' },
    { icon: '📐', label: '温度标准差', value: fmt(s.temperatureStd, 2), iconBg: '#f5f3ff', iconColor: '#8b5cf6' },
    { icon: '💧', label: '平均湿度', value: fmt(s.humidityAvg, 2, '%'), iconBg: '#f0f9ff', iconColor: '#0ea5e9' },
    { icon: '🔥', label: '燃气最大值', value: fmt(s.gasMax, 2, 'ppm'), iconBg: '#f5f3ff', iconColor: '#8b5cf6' },
    { icon: '📈', label: '波动程度', value: s.volatilityLevel, iconBg: '#fffbeb', iconColor: '#f59e0b' },
    { icon: '🌡', label: '温度变化率', value: fmt(s.temperatureChangeRate, 3, '℃/min'), iconBg: '#fff7ed', iconColor: '#f97316' },
    { icon: '💧', label: '湿度变化率', value: fmt(s.humidityChangeRate, 3, '%/min'), iconBg: '#f0f9ff', iconColor: '#0ea5e9' },
    { icon: '🔥', label: '燃气变化率', value: fmt(s.gasChangeRate, 3, 'ppm/min'), iconBg: '#f5f3ff', iconColor: '#8b5cf6' },
    { icon: '📉', label: '整体趋势', value: s.trend, iconBg: '#ecfdf5', iconColor: '#10b981' },
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
.summary-card { padding: 18px 22px; margin-bottom: 20px; font-size: 14px; color: #1f2937; line-height: 1.6; background: linear-gradient(135deg, #eff6ff, #f0f9ff); }
.stat-grid { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); margin-bottom: 14px; }
.trend-card { padding: 14px 18px; }
.trend-row { display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 8px; font-size: 13px; color: #64748b; }
.trend-row strong { color: #1f2937; }
.up { color: #ef4444; }
.down { color: #2563eb; }
.stable { color: #10b981; }
.trend-card p { font-size: 13px; color: #64748b; }
</style>
