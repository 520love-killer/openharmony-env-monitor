<template>
  <div class="page-shell">
    <PageHeader title="温度预测分析" subtitle="滑动平均、线性回归、指数平滑三种轻量算法综合预测" />

    <BaseCard class="summary-card">
      <strong>预测结论：</strong>
      {{ conclusionText }}
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

    <SectionHeader title="算法对比" />
    <div class="algo-grid">
      <BaseCard v-for="a in algorithms" :key="a.name" class="algo-card">
        <h4>{{ a.name }}</h4>
        <p class="algo-desc">{{ a.desc }}</p>
        <div class="algo-values">
          <div>
            <span>未来 5 分钟</span>
            <strong>{{ a.v5 }}</strong>
          </div>
          <div>
            <span>未来 10 分钟</span>
            <strong>{{ a.v10 }}</strong>
          </div>
        </div>
      </BaseCard>
    </div>

    <BaseCard class="algo-note">
      本预测使用滑动平均、线性回归和指数平滑三种轻量算法，适合小样本短期趋势估计，不是工业级精确温控模型。
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import MetricCard from '@/components/common/MetricCard.vue'
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
  if (!data.value?.success) return [{ icon: '⏳', label: '状态', value: '数据不足', iconBg: '#f3f4f6', iconColor: '#64748b' }]
  const d = data.value
  return [
    { icon: '🌡', label: '当前温度', value: fmt(d.currentTemperature, 2, '℃'), iconBg: '#fff7ed', iconColor: '#f97316' },
    { icon: '🔮', label: '未来 5 分钟', value: fmt(d.finalForecast5min, 2, '℃'), iconBg: '#f0f9ff', iconColor: '#0ea5e9' },
    { icon: '🔮', label: '未来 10 分钟', value: fmt(d.finalForecast10min, 2, '℃'), iconBg: '#eff6ff', iconColor: '#2563eb' },
    { icon: '📈', label: '趋势', value: d.trend, iconBg: '#ecfdf5', iconColor: '#10b981' },
    { icon: '✅', label: '置信度', value: d.confidence, iconBg: '#f5f3ff', iconColor: '#7c3aed' },
    { icon: '📊', label: '样本数', value: String(d.sampleCount), iconBg: '#eff6ff', iconColor: '#2563eb' },
  ]
})

const algorithms = computed(() => {
  if (!data.value?.success) return []
  const d = data.value
  return [
    {
      name: '滑动平均',
      desc: '取最近 5 条温度平均值平滑短期波动，结合变化率推算。',
      v5: fmt(d.movingAverageForecast5min, 2, '℃'),
      v10: fmt(d.movingAverageForecast10min, 2, '℃'),
    },
    {
      name: '线性回归',
      desc: '使用序号作为 x、温度作为 y，计算斜率和截距，判断整体趋势。',
      v5: fmt(d.linearRegressionForecast5min, 2, '℃'),
      v10: fmt(d.linearRegressionForecast10min, 2, '℃'),
    },
    {
      name: '指数平滑',
      desc: '使用 EWMA（α=0.3）强调最近数据对预测的影响。',
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
.summary-card { padding: 18px 22px; margin-bottom: 20px; font-size: 14px; color: #1f2937; line-height: 1.6; background: linear-gradient(135deg, #eff6ff, #f0f9ff); }
.stat-grid { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); margin-bottom: 14px; }
.algo-grid { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); margin-bottom: 14px; }
.algo-card { padding: 18px 20px; }
.algo-card h4 { font-size: 15px; font-weight: 600; color: #0f172a; margin-bottom: 4px; }
.algo-desc { font-size: 12px; color: #64748b; margin-bottom: 12px; line-height: 1.4; }
.algo-values { display: flex; gap: 20px; }
.algo-values div { text-align: center; }
.algo-values span { display: block; font-size: 11px; color: #64748b; }
.algo-values strong { font-size: 18px; font-weight: 700; color: #0f172a; }
.algo-note { padding: 14px 18px; font-size: 13px; color: #92400e; line-height: 1.6; background: #fffbeb; border-color: #fde68a; }
</style>
