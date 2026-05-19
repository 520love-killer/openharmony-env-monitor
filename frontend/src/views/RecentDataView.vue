<template>
  <div class="page-shell">
    <PageHeader title="最近 50 条数据" :subtitle="sourceLabel" />

    <div class="toolbar">
      <div class="toolbar-item">
        <StatusBadge type="info">{{ rows.length }}</StatusBadge>
        条记录
      </div>
      <div class="toolbar-item">
        最近更新：<strong>{{ lastTime }}</strong>
      </div>
      <div class="toolbar-item">
        数据源：<strong>{{ sourceLabel }}</strong>
      </div>
    </div>

    <BaseCard class="table-card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>设备 ID</th>
              <th>温度</th>
              <th>湿度</th>
              <th>燃气</th>
              <th>状态</th>
              <th>数据来源</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ fmt(row.createdAt) }}</td>
              <td>{{ row.deviceId }}</td>
              <td>{{ fmt(row.temperature, 1, '℃') }}</td>
              <td>{{ fmt(row.humidity, 1, '%') }}</td>
              <td>{{ fmt(row.gas, 1, 'ppm') }}</td>
              <td>
                <StatusBadge :type="row.status === 'WARNING' ? 'warning' : 'safe'">
                  {{ row.status }}
                </StatusBadge>
              </td>
              <td>
                <StatusBadge type="serial">{{ sourceMap[row.dataSource || source] || row.dataSource || source }}</StatusBadge>
              </td>
            </tr>
            <tr v-if="rows.length === 0">
              <td colspan="7" class="empty">等待 Hi3861 真实设备数据。</td>
            </tr>
          </tbody>
        </table>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { useAgentStore } from '@/stores/agent'
import { sensorApi } from '@/api/sensor'
import type { SensorData } from '@/types'

const agentStore = useAgentStore()
const source = computed(() => agentStore.source)

const rows = ref<SensorData[]>([])

const sourceMap: Record<string, string> = {
  REAL_SERIAL: '真实串口数据',
  REAL_MQTT: '真实 MQTT 数据',
  MOCK: '模拟演示数据',
  ALL: '全部数据',
}

const sourceLabel = computed(() => sourceMap[source.value] || source.value)
const lastTime = computed(() => rows.value.length > 0 ? fmt(rows.value[0].createdAt) : '--')

async function load() {
  try {
    const res = await sensorApi.recent(source.value, 50)
    rows.value = Array.isArray(res.data) ? res.data : []
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
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; padding: 12px 0; font-size: 13px; color: #64748b; margin-bottom: 8px; }
.toolbar-item { display: flex; align-items: center; gap: 6px; }
.table-card { overflow: hidden; }
.table-wrap { overflow-x: auto; }
table { width: 100%; min-width: 920px; border-collapse: collapse; }
th, td { border-bottom: 1px solid #e5edf7; padding: 11px 10px; text-align: left; }
th { color: #64748b; font-size: 13px; background: #f8fbff; font-weight: 600; }
td { line-height: 1.45; font-size: 13px; }
tr:last-child td { border-bottom: 0; }
tr:hover td { background: #f8fbff; }
.empty { text-align: center; color: #64748b; padding: 24px; }
</style>
