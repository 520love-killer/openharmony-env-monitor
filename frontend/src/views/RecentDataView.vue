<template>
  <div class="page-shell">
    <PageHeader title="最近 50 条数据" :subtitle="sourceLabel" />

    <div class="toolbar">
      <div class="toolbar-left">
        <StatusBadge type="info">{{ rows.length }} 条</StatusBadge>
        <span class="toolbar-sep">|</span>
        <span>更新：<strong>{{ lastTime }}</strong></span>
        <span class="toolbar-sep">|</span>
        <span>数据源：<strong>{{ sourceLabel }}</strong></span>
      </div>
      <button class="refresh-btn" @click="load">刷新</button>
    </div>

    <BaseCard class="table-card">
      <div class="table-wrap scrollbar-thin">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>设备 ID</th>
              <th>温度</th>
              <th>湿度</th>
              <th>燃气</th>
              <th>状态</th>
              <th>来源</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td class="td-time">{{ fmt(row.createdAt) }}</td>
              <td class="td-mono">{{ row.deviceId }}</td>
              <td class="td-num">{{ fmt(row.temperature, 1, '℃') }}</td>
              <td class="td-num">{{ fmt(row.humidity, 1, '%') }}</td>
              <td class="td-num">{{ fmt(row.gas, 1, 'ppm') }}</td>
              <td>
                <StatusBadge :type="row.status === 'WARNING' ? 'warning' : 'safe'" :dot="row.status === 'SAFE'">
                  {{ row.status }}
                </StatusBadge>
              </td>
              <td>
                <StatusBadge type="serial">{{ sourceMap[row.dataSource || source] || row.dataSource || source }}</StatusBadge>
              </td>
            </tr>
            <tr v-if="rows.length === 0">
              <td colspan="7" class="empty">等待 Hi3861 真实设备数据...</td>
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
  REAL_SERIAL: 'REAL_SERIAL',
  REAL_MQTT: 'REAL_MQTT',
  MOCK: 'MOCK',
  ALL: 'ALL',
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
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0; font-size: 13px; color: var(--text-muted); margin-bottom: 8px;
}
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.toolbar-sep { color: rgba(148,163,184,0.20); }
.toolbar strong { color: var(--text-primary); font-weight: 600; }
.refresh-btn {
  background: rgba(56, 189, 248, 0.10);
  border: 1px solid rgba(56, 189, 248, 0.20);
  color: var(--blue);
  padding: 5px 14px; border-radius: 8px; cursor: pointer;
  font-size: 12px; font-weight: 500; transition: all 0.2s;
}
.refresh-btn:hover { background: rgba(56, 189, 248, 0.18); }

.table-card { overflow: hidden; }
.table-wrap { overflow-x: auto; overflow-y: auto; max-height: 600px; }
table { width: 100%; min-width: 860px; border-collapse: collapse; }
th {
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  padding: 13px 12px; text-align: left;
  color: var(--text-muted); font-size: 11px;
  font-weight: 600; letter-spacing: 0.5px;
  text-transform: uppercase;
  background: rgba(15, 23, 42, 0.50);
  position: sticky; top: 0; z-index: 1;
}
td {
  border-bottom: 1px solid rgba(148, 163, 184, 0.08);
  padding: 11px 12px; line-height: 1.4; font-size: 13px;
  color: var(--text-primary);
}
tr:hover td { background: rgba(56, 189, 248, 0.04); }
tr:last-child td { border-bottom: 0; }
.td-time { white-space: nowrap; font-size: 12px; }
.td-mono { font-family: "Cascadia Code", "Fira Code", monospace; font-size: 12px; color: var(--text-muted); }
.td-num { font-weight: 600; font-variant-numeric: tabular-nums; }
.empty { text-align: center; color: var(--text-dim); padding: 32px; }
</style>
