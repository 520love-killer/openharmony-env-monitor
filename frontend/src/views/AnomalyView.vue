<template>
  <div class="page-shell">
    <PageHeader title="异常检测与风险诊断" subtitle="阈值超限 · 突变检测 · 连续升高 · 长期无变化" />

    <BaseCard class="risk-card" :class="riskCardClass">
      <div class="risk-header">
        <StatusBadge :type="anomaly?.hasAnomaly ? 'danger' : 'safe'" dot>
          {{ anomaly?.hasAnomaly ? '存在异常' : '系统安全' }}
        </StatusBadge>
        <strong>{{ anomaly?.anomalyCount || 0 }} 个异常</strong>
      </div>
      <p>{{ anomaly?.message || '等待异常检测结果...' }}</p>
    </BaseCard>

    <div v-if="items.length" class="anomaly-list">
      <BaseCard v-for="(item, i) in items" :key="i" class="anomaly-item" :class="`anomaly-${item.level?.toLowerCase()}`">
        <div class="anomaly-head">
          <strong>{{ typeMap[item.type] || item.type }}</strong>
          <StatusBadge :type="levelBadgeType(item.level)">{{ item.level }}</StatusBadge>
        </div>
        <p class="anomaly-reason">{{ item.reason }}</p>
        <span class="anomaly-time">{{ fmt(item.time) }}</span>
        <p class="anomaly-suggestion">💡 {{ item.suggestion }}</p>
      </BaseCard>
    </div>
    <EmptyState v-else text="暂无异常记录 · 系统运行正常" />

    <SectionHeader title="检测规则引擎" />
    <div class="rules-grid">
      <BaseCard v-for="r in rules" :key="r.title" class="rule-card">
        <div class="rule-icon-wrap" :style="{ background: r.iconBg }">
          <span class="rule-icon">{{ r.icon }}</span>
        </div>
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

const riskCardClass = computed(() => {
  if (!anomaly.value) return ''
  if (anomaly.value.hasAnomaly) {
    const hasHigh = items.value.some(i => i.level?.toLowerCase() === 'high')
    if (hasHigh) return 'risk-high'
    return 'risk-warn'
  }
  return 'risk-safe'
})

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

function levelBadgeType(l?: string) {
  if (!l) return 'info'
  const lv = l.toLowerCase()
  if (lv === 'high') return 'danger'
  if (lv === 'medium') return 'warning'
  return 'info'
}

const rules = [
  { icon: '🚦', title: '阈值检测', desc: '温度 > 50℃、湿度 > 90%、燃气 > 500ppm 触发告警', iconBg: 'rgba(239,68,68,0.12)' },
  { icon: '📈', title: '突变检测', desc: '相邻数据变化超过阈值时触发，识别传感器异常跳变', iconBg: 'rgba(245,158,11,0.12)' },
  { icon: '⬆', title: '连续升高检测', desc: '最近 5 条数据持续上升时发出预警', iconBg: 'rgba(249,115,22,0.12)' },
  { icon: '⏸', title: '长期无变化检测', desc: '最近 10 条数据几乎不变时提示传感器可能故障', iconBg: 'rgba(56,189,248,0.12)' },
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
.risk-card { padding: 18px 22px; margin-bottom: 14px; }
.risk-safe { border-color: rgba(34, 197, 94, 0.25); }
.risk-warn { border-color: rgba(245, 158, 11, 0.30); box-shadow: 0 0 24px rgba(245, 158, 11, 0.08); }
.risk-high { border-color: rgba(239, 68, 68, 0.35); box-shadow: 0 0 24px rgba(239, 68, 68, 0.10); }
.risk-header { display: flex; align-items: center; gap: 14px; margin-bottom: 10px; }
.risk-header strong { font-size: 22px; color: var(--text-heading); }
.risk-card p { color: var(--text-muted); line-height: 1.6; font-size: 13px; }

.anomaly-list { display: grid; gap: 10px; margin-bottom: 20px; }
.anomaly-item { padding: 16px 20px; }
.anomaly-high { border-left: 3px solid #ef4444; }
.anomaly-medium { border-left: 3px solid #f59e0b; }
.anomaly-low { border-left: 3px solid #38bdf8; }
.anomaly-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-bottom: 8px; }
.anomaly-head strong { color: var(--text-heading); font-size: 14px; }
.anomaly-reason { color: var(--text-muted); line-height: 1.6; font-size: 13px; }
.anomaly-time { display: block; margin: 8px 0; color: var(--text-dim); font-size: 11px; }
.anomaly-suggestion { color: var(--text-muted); line-height: 1.6; font-size: 12px; font-style: normal; }

.rules-grid { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
.rule-card { padding: 18px 20px; display: flex; flex-direction: column; gap: 8px; }
.rule-icon-wrap {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.rule-icon { font-size: 20px; }
.rule-card strong { font-size: 14px; font-weight: 600; color: var(--text-heading); }
.rule-card p { font-size: 12px; color: var(--text-muted); line-height: 1.5; }
</style>
