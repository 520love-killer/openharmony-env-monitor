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
  tooltip: { trigger: 'axis' },
  grid: { left: 10, right: 10, top: 10, bottom: 20, containLabel: true },
  xAxis: { type: 'category', data: props.labels, axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value', axisLabel: { fontSize: 11 } },
  series: [{
    type: 'line',
    data: props.values,
    smooth: true,
    symbol: 'circle',
    symbolSize: 4,
    lineStyle: { color: props.color, width: 2 },
    itemStyle: { color: props.color },
    areaStyle: { color: props.color, opacity: 0.08 },
  }],
}))
</script>

<style scoped>
.chart-card { padding: 14px; display: flex; flex-direction: column; min-width: 0; height: 320px; }
.chart-title { font-size: 15px; font-weight: 600; margin-bottom: 10px; color: #0f172a; flex-shrink: 0; }
.chart-wrap { flex: 1; min-height: 0; position: relative; }
.chart { width: 100%; height: 100%; }
</style>
