<template>
  <div class="page-shell">
    <PageHeader title="温度预测分析" subtitle="滑动平均 · 线性回归 · 指数平滑 — 三种轻量算法综合预测" />

    <BaseCard class="summary-card">
      <div class="summary-head">
        <span class="summary-icon">◈</span>
        <strong>预测引擎输出</strong>
      </div>
      <p>{{ conclusionText }}</p>
    </BaseCard>

    <div class="forecast-big-grid">
      <div class="forecast-big-item">
        <span class="big-label">当前温度</span>
        <span class="big-value" style="color: #f97316; text-shadow: 0 0 24px rgba(249,115,22,0.3);">{{ metrics.length > 1 ? metrics[0].value : '--' }}</span>
      </div>
      <div class="forecast-big-item">
        <span class="big-label">未来 5 分钟</span>
        <span class="big-value" style="color: #38bdf8; text-shadow: 0 0 24px rgba(56,189,248,0.3);">{{ metrics.length > 1 ? metrics[1].value : '--' }}</span>
      </div>
      <div class="forecast-big-item">
        <span class="big-label">未来 10 分钟</span>
        <span class="big-value" style="color: #a78bfa; text-shadow: 0 0 24px rgba(139,92,246,0.3);">{{ metrics.length > 2 ? metrics[2].value : '--' }}</span>
      </div>
    </div>

    <div class="stat-grid">
      <div v-for="m in metrics.slice(3)" :key="m.label" class="stat-item">
        <div class="stat-icon" :style="{ background: m.iconBg, color: m.iconColor }">{{ m.icon }}</div>
        <span class="stat-label">{{ m.label }}</span>
        <strong class="stat-value">{{ m.value }}</strong>
      </div>
    </div>

    <SectionHeader title="算法对比" />
    <div class="algo-grid">
      <BaseCard v-for="a in algorithms" :key="a.name" class="algo-card">
        <div class="algo-head">
          <span class="algo-icon">{{ a.icon }}</span>
          <h4>{{ a.name }}</h4>
        </div>
        <p class="algo-desc">{{ a.desc }}</p>
        <div class="algo-values">
          <div class="algo-val-item">
            <span>未来 5 分钟</span>
            <strong>{{ a.v5 }}</strong>
          </div>
          <div class="algo-val-item">
            <span>未来 10 分钟</span>
            <strong>{{ a.v10 }}</strong>
          </div>
        </div>
      </BaseCard>
    </div>

    <BaseCard class="algo-note">
      <span class="note-icon">ℹ</span>
      <span>本预测使用滑动平均、线性回归和指数平滑三种轻量算法，适合小样本短期趋势估计，不是工业级精确温控模型。</span>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import { useAgentStore } from '@/stores/agent'
import { forecastApi } from '@/api/forecast'
import type { ForecastResponse } from '@/types'

const agentStore = useAgentStore()
const source = computed(() => agentStore.source)

const data = ref<ForecastResponse | null>(null)

const conclusionText = computed(() => {
  if (!data.value?.success) return data.value?.message || '等待预测结果...'
  const d = data.value
  return `当前温度 ${fmt(d.currentTemperature, 2, '℃')}，未来 10 分钟预计 ${fmt(d.finalForecast10min, 2, '℃')}，趋势 ${d.trend}（置信度：${d.confidence}）。基于 ${d.sampleCount} 条真实数据计算。`
})

const metrics = computed(() => {
  if (!data.value?.success) return [{ icon: '⏳', label: '状态', value: '数据不足', iconBg: 'rgba(148,163,184,0.10)', iconColor: '#94a3b8' }]
  const d = data.value
  return [
    { icon: '🌡', label: '当前温度', value: fmt(d.currentTemperature, 2, '℃'), iconBg: 'rgba(249,115,22,0.12)', iconColor: '#f97316' },
    { icon: '🔮', label: '未来 5 分钟', value: fmt(d.finalForecast5min, 2, '℃'), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '🔮', label: '未来 10 分钟', value: fmt(d.finalForecast10min, 2, '℃'), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
    { icon: '📈', label: '趋势', value: d.trend, iconBg: 'rgba(34,197,94,0.10)', iconColor: '#22c55e' },
    { icon: '✅', label: '置信度', value: d.confidence, iconBg: 'rgba(139,92,246,0.10)', iconColor: '#a78bfa' },
    { icon: '📊', label: '样本数', value: String(d.sampleCount), iconBg: 'rgba(56,189,248,0.10)', iconColor: '#38bdf8' },
  ]
})

const algorithms = computed(() => {
  if (!data.value?.success) return []
  const d = data.value
  return [
    {
      icon: '📐', name: '滑动平均模型',
      desc: '取最近 5 条温度平均值平滑短期波动，结合变化率推算未来趋势。',
      v5: fmt(d.movingAverageForecast5min, 2, '℃'),
      v10: fmt(d.movingAverageForecast10min, 2, '℃'),
    },
    {
      icon: '📈', name: '线性回归模型',
      desc: '使用序号作为 x、温度作为 y，计算斜率和截距，判断整体趋势方向。',
      v5: fmt(d.linearRegressionForecast5min, 2, '℃'),
      v10: fmt(d.linearRegressionForecast10min, 2, '℃'),
    },
    {
      icon: '〰', name: '指数平滑模型',
      desc: '使用 EWMA（α=0.3）加权平均，强调最近数据对预测结果的影响。',
      v5: fmt(d.exponentialSmoothingForecast5min, 2, '℃'),
      v10: fmt(d.exponentialSmoothingForecast10min, 2, '℃'),
    },
  ]
})

async function load() {
  try {
    const res = await forecastApi.temperature(source.value)
    data.value = res.data
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

.forecast-big-grid {
  display: grid; gap: 16px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 20px;
}
.forecast-big-item {
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.72), rgba(15, 23, 42, 0.62));
  backdrop-filter: blur(14px);
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 22px;
  padding: 24px;
  display: flex; flex-direction: column; gap: 8px;
  text-align: center;
}
.big-label { font-size: 12px; color: var(--text-dim); font-weight: 500; letter-spacing: 0.5px; text-transform: uppercase; }
.big-value { font-size: 36px; font-weight: 800; line-height: 1.1; }

.stat-grid { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); margin-bottom: 20px; }
.stat-item {
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.72), rgba(15, 23, 42, 0.62));
  backdrop-filter: blur(14px);
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 18px;
  padding: 16px;
  display: flex; flex-direction: column; gap: 6px;
  transition: all 0.2s;
}
.stat-item:hover { border-color: rgba(56,189,248,0.25); transform: translateY(-1px); }
.stat-icon { width: 34px; height: 34px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.stat-label { font-size: 11px; color: var(--text-dim); font-weight: 500; }
.stat-value { font-size: 20px; font-weight: 800; color: var(--text-heading); line-height: 1.2; }

.algo-grid { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); margin-bottom: 16px; }
.algo-card { padding: 20px 22px; }
.algo-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.algo-icon { font-size: 20px; }
.algo-card h4 { font-size: 15px; font-weight: 600; color: var(--text-heading); }
.algo-desc { font-size: 12px; color: var(--text-muted); margin-bottom: 14px; line-height: 1.5; }
.algo-values { display: flex; gap: 24px; }
.algo-val-item { flex: 1; text-align: center; padding: 12px; background: rgba(2,6,23,0.35); border-radius: 12px; border: 1px solid rgba(148,163,184,0.10); }
.algo-val-item span { display: block; font-size: 10px; color: var(--text-dim); margin-bottom: 4px; }
.algo-val-item strong { font-size: 20px; font-weight: 800; color: var(--text-heading); }

.algo-note {
  padding: 14px 20px; font-size: 13px; color: var(--amber); line-height: 1.6;
  display: flex; align-items: flex-start; gap: 10px;
  background: rgba(245, 158, 11, 0.06);
  border-color: rgba(245, 158, 11, 0.20);
}
.note-icon { flex-shrink: 0; font-size: 16px; }
</style>
