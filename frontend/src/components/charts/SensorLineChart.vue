<template>
  <BaseCard class="chart-card">
    <h3 class="chart-title">{{ title }}</h3>
    <div class="chart-wrap">
      <v-chart v-if="hasData" class="chart" :option="option" autoresize />
      <EmptyState v-else text="等待数据..." />
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import BaseCard from '@/components/common/BaseCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{
  title: string
  labels: string[]
  values: (number | null)[]
  color: string
}>()

const hasData = computed(() => props.values.length > 0)

const option = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    borderColor: 'rgba(56, 189, 248, 0.3)',
    textStyle: { color: '#e5f0ff', fontSize: 12 },
  },
  grid: { left: 8, right: 16, top: 10, bottom: 20, containLabel: true },
  xAxis: {
    type: 'category',
    data: props.labels,
    axisLabel: { fontSize: 10, color: '#94a3b8' },
    axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)' } },
    axisTick: { show: false },
  },
  yAxis: {
    type: 'value',
    axisLabel: { fontSize: 10, color: '#94a3b8' },
    splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.08)' } },
    axisLine: { show: false },
    axisTick: { show: false },
  },
  series: [{
    type: 'line',
    data: props.values,
    smooth: true,
    symbol: 'circle',
    symbolSize: 5,
    lineStyle: { color: props.color, width: 2.5, shadowBlur: 8, shadowColor: props.color },
    itemStyle: { color: props.color },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: props.color + '30' },
          { offset: 1, color: props.color + '02' },
        ],
      },
    },
  }],
}))
</script>

<style scoped>
.chart-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 330px;
}
.chart-title {
  font-size: 14px; font-weight: 600; margin-bottom: 10px;
  color: var(--text-heading); flex-shrink: 0;
}
.chart-wrap { flex: 1; min-height: 0; position: relative; }
.chart { width: 100%; height: 100%; }
</style>
