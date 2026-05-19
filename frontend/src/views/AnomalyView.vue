<template>
  <div class="page-shell">
    <PageHeader title="异常检测与风险诊断" subtitle="检测阈值超限、突变、连续升高和长期无变化" />

    <BaseCard class="risk-card">
      <div class="risk-header">
        <StatusBadge :type="anomaly?.hasAnomaly ? 'warning' : 'safe'" dot>
          {{ anomaly?.hasAnomaly ? '存在异常' : '未发现异常' }}
        </StatusBadge>
        <strong>{{ anomaly?.anomalyCount || 0 }} 个异常</strong>
      </div>
      <p>{{ anomaly?.message || '等待异常检测结果...' }}</p>
    </BaseCard>

    <div v-if="items.length" class="anomaly-list">
      <BaseCard v-for="(item, i) in items" :key="i" class="anomaly-item">
        <div class="anomaly-head">
          <strong>{{ typeMap[item.type] || item.type }}</strong>
          <StatusBadge :type="levelType(item.level)">{{ item.level }}</StatusBadge>
        </div>
        <p>{{ item.reason }}</p>
        <span class="anomaly-time">{{ fmt(item.time) }}</span>
        <em>{{ item.suggestion }}</em>
      </BaseCard>
    </div>
    <EmptyState v-else text="暂无异常记录" />

    <SectionHeader title="检测规则" />
    <div class="rules-grid">
      <BaseCard v-for="r in rules" :key="r.title" class="rule-card">
        <span class="rule-icon">{{ r.icon }}</span>
        <strong>{{ r.title }}</strong>
        <p>{{ r.desc }}</p>
      </BaseCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useAgentStore } from '@/stores/agent'
import { anomalyApi } from '@/api/anomaly'
import type { AnomalyResponse } from '@/types'

const agentStore = useAgentStore()
const source = computed(() => agentStore.source)

const anomaly = ref<AnomalyResponse | null>(null)

const items = computed(() => anomaly.value?.items || [])

const typeMap: Record<string, string> = {
  HUMIDITY_STUCK: '湿度长期无变化',
  GAS_STUCK: '燃气读数长期无变化',
  TEMP_THRESHOLD: '温度超阈值',
  GAS_THRESHOLD: '燃气超阈值',
  TEMP_SPIKE: '温度突变',
  GAS_SPIKE: '燃气突变',
  TEMP_RISING: '温度连续升高',
  GAS_RISING: '燃气连续升高',
}

function levelType(l?: string) {
  if (!l) return 'info'
  const lv = l.toLowerCase()
  if (lv === 'high') return 'warning'
  if (lv === 'medium') return 'safe'
  return 'info'
}

const rules = [
  { icon: '🚦', title: '阈值检测', desc: '温度 > 50℃、湿度 > 90%、燃气 > 500ppm 触发告警' },
  { icon: '📈', title: '突变检测', desc: '相邻数据变化超过阈值时触发，识别传感器异常跳变' },
  { icon: '⬆', title: '连续升高检测', desc: '最近 5 条数据持续上升时发出预警' },
  { icon: '⏸', title: '长期无变化检测', desc: '最近 10 条数据几乎不变时提示传感器可能故障' },
]

async function load() {
  try {
    const res = await anomalyApi.detect(source.value)
    anomaly.value = res.data
  } catch { /* ignore */ }
}

watch(source, load)
onMounted(load)

function fmt(v: string) {
  return new Date(v).toLocaleString()
}
</script>

<style scoped>
.risk-card { padding: 16px; margin-bottom: 14px; }
.risk-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.risk-header strong { font-size: 20px; }
.risk-card p { color: #64748b; line-height: 1.55; }
.anomaly-list { display: grid; gap: 10px; margin-bottom: 20px; }
.anomaly-item { padding: 14px; }
.anomaly-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-bottom: 8px; }
.anomaly-item p { color: #64748b; line-height: 1.55; }
.anomaly-time { display: block; margin: 7px 0; color: #64748b; font-size: 13px; }
.anomaly-item em { display: block; color: #64748b; line-height: 1.55; font-style: normal; font-size: 12px; }
.rules-grid { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
.rule-card { padding: 16px; }
.rule-icon { font-size: 22px; display: block; margin-bottom: 8px; }
.rule-card strong { font-size: 14px; font-weight: 600; color: #0f172a; display: block; margin-bottom: 4px; }
.rule-card p { font-size: 12px; color: #64748b; line-height: 1.5; }
</style>
