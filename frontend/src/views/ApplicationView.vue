<template>
  <div class="page-shell">
    <PageHeader title="应用场景" subtitle="将 Hi3861 环境监测数据应用到农业、工业、实验与智能空调场景" />

    <!-- User Role Selection -->
    <SectionHeader title="用户身份" />
    <div class="role-grid">
      <div
        v-for="role in roles"
        :key="role.value"
        class="select-card"
        :class="{ active: scenarioStore.settings.userRole === role.value }"
        @click="selectRole(role.value)"
      >
        <div class="select-icon">{{ role.icon }}</div>
        <div class="select-label">{{ role.label }}</div>
        <div class="select-desc">{{ role.desc }}</div>
      </div>
    </div>

    <!-- Scenario Selection -->
    <SectionHeader title="应用场景" />
    <div class="scenario-grid">
      <div
        v-for="s in scenarios"
        :key="s.value"
        class="select-card scenario-card"
        :class="{ active: scenarioStore.settings.scenario === s.value }"
        @click="selectScenario(s.value)"
      >
        <div class="select-icon">{{ s.icon }}</div>
        <div class="select-label">{{ s.label }}</div>
        <div class="select-desc">{{ s.desc }}</div>
      </div>
    </div>

    <!-- Parameter Configuration -->
    <SectionHeader title="场景参数" />
    <BaseCard class="config-panel">
      <!-- Agriculture params -->
      <div v-if="scenarioStore.settings.scenario === 'AGRICULTURE_GREENHOUSE'" class="config-group">
        <label>作物类型</label>
        <div class="chip-row">
          <button
            v-for="c in crops"
            :key="c.value"
            class="chip"
            :class="{ active: scenarioStore.settings.crop === c.value }"
            @click="scenarioStore.updateSettings({ crop: c.value })"
          >
            {{ c.label }}
          </button>
        </div>
      </div>

      <!-- AC params -->
      <div v-if="scenarioStore.settings.scenario === 'SMART_AIR_CONDITIONER'" class="config-group">
        <label>房间类型</label>
        <div class="chip-row">
          <button
            v-for="r in roomTypes"
            :key="r.value"
            class="chip"
            :class="{ active: scenarioStore.settings.roomType === r.value }"
            @click="scenarioStore.updateSettings({ roomType: r.value })"
          >
            {{ r.label }}
          </button>
        </div>
      </div>

      <!-- Custom params -->
      <div v-if="scenarioStore.settings.scenario === 'CUSTOM_SCENARIO'" class="config-group">
        <label>自定义阈值</label>
        <div class="input-row">
          <div class="input-group">
            <span>温度最小值</span>
            <input v-model.number="scenarioStore.settings.customTempMin" type="number" step="0.5" />
          </div>
          <div class="input-group">
            <span>温度最大值</span>
            <input v-model.number="scenarioStore.settings.customTempMax" type="number" step="0.5" />
          </div>
          <div class="input-group">
            <span>湿度最小值</span>
            <input v-model.number="scenarioStore.settings.customHumidityMin" type="number" step="1" />
          </div>
          <div class="input-group">
            <span>湿度最大值</span>
            <input v-model.number="scenarioStore.settings.customHumidityMax" type="number" step="1" />
          </div>
          <div class="input-group">
            <span>燃气上限</span>
            <input v-model.number="scenarioStore.settings.customGasMax" type="number" step="1" />
          </div>
        </div>
      </div>

      <div class="config-actions">
        <button class="btn-primary" :disabled="loading" @click="runAnalysis">
          <span v-if="loading" class="spin">⟳</span>
          <span v-else>开始分析</span>
        </button>
      </div>
    </BaseCard>

    <!-- Error state -->
    <EmptyState v-if="error" :text="error" />

    <!-- Score Card -->
    <template v-if="result">
      <SectionHeader title="环境评分" />
      <div class="score-grid">
        <BaseCard class="score-main">
          <div class="score-big" :class="scoreClass">{{ result.score }}</div>
          <div class="score-level">
            <StatusBadge :type="levelBadgeType">{{ result.levelName }}</StatusBadge>
          </div>
          <div class="score-meta">
            <span>场景：{{ result.scenarioName }}</span>
            <span>身份：{{ result.userRoleName }}</span>
            <span>置信度：{{ result.confidence }}</span>
          </div>
          <div class="score-summary">{{ result.summary }}</div>
        </BaseCard>

        <BaseCard class="score-breakdown">
          <h4>评分明细</h4>
          <div class="breakdown-list">
            <div class="breakdown-item">
              <span>温度得分</span>
              <div class="breakdown-bar">
                <div class="bar-fill" :style="{ width: result.scoreBreakdown.temperatureScore + '%', background: barColor(result.scoreBreakdown.temperatureScore) }" />
              </div>
              <span>{{ result.scoreBreakdown.temperatureScore }}</span>
            </div>
            <div class="breakdown-item">
              <span>湿度得分</span>
              <div class="breakdown-bar">
                <div class="bar-fill" :style="{ width: result.scoreBreakdown.humidityScore + '%', background: barColor(result.scoreBreakdown.humidityScore) }" />
              </div>
              <span>{{ result.scoreBreakdown.humidityScore }}</span>
            </div>
            <div class="breakdown-item">
              <span>燃气安全</span>
              <div class="breakdown-bar">
                <div class="bar-fill" :style="{ width: result.scoreBreakdown.gasScore + '%', background: barColor(result.scoreBreakdown.gasScore) }" />
              </div>
              <span>{{ result.scoreBreakdown.gasScore }}</span>
            </div>
            <div class="breakdown-item">
              <span>趋势稳定</span>
              <div class="breakdown-bar">
                <div class="bar-fill" :style="{ width: result.scoreBreakdown.trendScore + '%', background: barColor(result.scoreBreakdown.trendScore) }" />
              </div>
              <span>{{ result.scoreBreakdown.trendScore }}</span>
            </div>
            <div class="breakdown-item">
              <span>异常风险</span>
              <div class="breakdown-bar">
                <div class="bar-fill" :style="{ width: result.scoreBreakdown.anomalyScore + '%', background: barColor(result.scoreBreakdown.anomalyScore) }" />
              </div>
              <span>{{ result.scoreBreakdown.anomalyScore }}</span>
            </div>
          </div>
        </BaseCard>
      </div>

      <!-- Metrics Status -->
      <SectionHeader title="关键指标" />
      <div class="metric-status-grid">
        <BaseCard class="metric-status-item">
          <div class="ms-label">温度状态</div>
          <StatusBadge :type="metricType(result.metrics.temperatureStatus)">{{ metricLabel(result.metrics.temperatureStatus) }}</StatusBadge>
        </BaseCard>
        <BaseCard class="metric-status-item">
          <div class="ms-label">湿度状态</div>
          <StatusBadge :type="metricType(result.metrics.humidityStatus)">{{ metricLabel(result.metrics.humidityStatus) }}</StatusBadge>
        </BaseCard>
        <BaseCard class="metric-status-item">
          <div class="ms-label">燃气状态</div>
          <StatusBadge :type="metricType(result.metrics.gasStatus)">{{ metricLabel(result.metrics.gasStatus) }}</StatusBadge>
        </BaseCard>
        <BaseCard class="metric-status-item">
          <div class="ms-label">趋势状态</div>
          <StatusBadge :type="metricType(result.metrics.trendStatus)">{{ trendLabel(result.metrics.trendStatus) }}</StatusBadge>
        </BaseCard>
      </div>

      <!-- Risks -->
      <SectionHeader v-if="result.risks.length > 0" title="风险提示" />
      <div v-if="result.risks.length > 0" class="risk-grid">
        <BaseCard v-for="(risk, i) in result.risks" :key="i" class="risk-card">
          <div class="risk-header">
            <StatusBadge :type="risk.level === 'HIGH' ? 'danger' : 'warning'">{{ risk.level }}</StatusBadge>
          </div>
          <div class="risk-message">{{ risk.message }}</div>
        </BaseCard>
      </div>

      <!-- Advices -->
      <SectionHeader v-if="result.advices.length > 0" title="推荐操作" />
      <div v-if="result.advices.length > 0" class="advice-grid">
        <BaseCard v-for="(advice, i) in result.advices" :key="i" class="advice-card">
          <div class="advice-title">{{ advice.title }}</div>
          <div class="advice-content">{{ advice.content }}</div>
        </BaseCard>
      </div>

      <!-- Algorithm Notes -->
      <SectionHeader title="算法说明" />
      <BaseCard class="algo-card">
        <ul class="algo-list">
          <li v-for="(note, i) in result.algorithmNotes" :key="i">{{ note }}</li>
        </ul>
      </BaseCard>

      <!-- Agent CTA -->
      <BaseCard class="agent-cta">
        <div class="cta-text">需要更详细的场景化分析？询问温度计 Agent</div>
        <button class="btn-primary" @click="askAgent">
          询问 Agent
        </button>
      </BaseCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useScenarioStore } from '@/stores/scenario'
import { useAgentStore } from '@/stores/agent'
import { scenarioApi } from '@/api/scenario'
import type { ScenarioAnalysis } from '@/types'

const router = useRouter()
const scenarioStore = useScenarioStore()
const agentStore = useAgentStore()

const loading = ref(false)
const error = ref<string | null>(null)
const result = ref<ScenarioAnalysis | null>(scenarioStore.lastAnalysis)

const roles = [
  { value: 'STUDENT', label: '学生 / 实验学习者', icon: '🎓', desc: '关注实验过程、数据理解、报告生成' },
  { value: 'FARMER', label: '农业种植者', icon: '🌱', desc: '关注温室环境、作物生长、通风浇水' },
  { value: 'ENGINEER', label: '工程技术人员', icon: '🔧', desc: '关注设备运行、安全阈值、异常报警' },
  { value: 'AC_USER', label: '空调使用者', icon: '❄️', desc: '关注室内舒适度、降温效果、调节建议' },
  { value: 'RESEARCHER', label: '研究分析者', icon: '🔬', desc: '关注统计分析、预测算法、数据波动' },
  { value: 'CUSTOM', label: '自定义用户', icon: '⚙️', desc: '自定义阈值和偏好' },
]

const scenarios = [
  { value: 'GENERAL_MONITOR', label: '通用环境监测', icon: '🌍', desc: '标准环境安全评估' },
  { value: 'AGRICULTURE_GREENHOUSE', label: '农业温室', icon: '🌿', desc: '作物适宜性分析' },
  { value: 'SMART_AIR_CONDITIONER', label: '智能空调', icon: '🏠', desc: '室内舒适度评估' },
  { value: 'INDUSTRIAL_SAFETY', label: '工业安全', icon: '🏭', desc: '设备环境与燃气安全' },
  { value: 'LAB_ENVIRONMENT', label: '实验室环境', icon: '🧪', desc: '实验数据稳定性' },
  { value: 'CUSTOM_SCENARIO', label: '自定义场景', icon: '🔧', desc: '自定义阈值范围' },
]

const crops = [
  { value: 'vegetable', label: '普通蔬菜' },
  { value: 'tomato', label: '番茄' },
  { value: 'strawberry', label: '草莓' },
  { value: 'cucumber', label: '黄瓜' },
]

const roomTypes = [
  { value: 'dormitory', label: '宿舍' },
  { value: 'classroom', label: '教室' },
  { value: 'bedroom', label: '卧室' },
  { value: 'office', label: '办公室' },
]

function selectRole(value: string) {
  scenarioStore.updateSettings({ userRole: value })
}

function selectScenario(value: string) {
  scenarioStore.updateSettings({ scenario: value })
}

const scoreClass = computed(() => {
  if (!result.value) return ''
  const s = result.value.score
  if (s >= 90) return 'score-excellent'
  if (s >= 75) return 'score-good'
  if (s >= 60) return 'score-normal'
  return 'score-warning'
})

const levelBadgeType = computed(() => {
  if (!result.value) return 'info'
  const l = result.value.level
  if (l === 'EXCELLENT') return 'safe'
  if (l === 'GOOD') return 'primary'
  if (l === 'NORMAL') return 'warning'
  return 'danger'
})

function barColor(score: number) {
  if (score >= 80) return 'var(--green)'
  if (score >= 60) return 'var(--amber)'
  return 'var(--red)'
}

function metricType(status: string) {
  if (status === 'NORMAL' || status === 'SAFE' || status === 'STABLE') return 'safe'
  if (status === 'WARNING' || status === 'SLIGHT_CHANGE') return 'warning'
  return 'danger'
}

function metricLabel(status: string) {
  const map: Record<string, string> = {
    NORMAL: '正常', WARNING: '警告', CRITICAL: '危急',
    SAFE: '安全', UNKNOWN: '未知',
  }
  return map[status] || status
}

function trendLabel(status: string) {
  const map: Record<string, string> = {
    STABLE: '稳定', SLIGHT_CHANGE: '轻微变化', RISING: '上升中', FALLING: '下降中', UNKNOWN: '未知',
  }
  return map[status] || status
}

async function runAnalysis() {
  loading.value = true
  error.value = null
  try {
    const s = scenarioStore.settings
    const res = await scenarioApi.analysis({
      scenario: s.scenario,
      userRole: s.userRole,
      source: agentStore.source,
      limit: 50,
      crop: s.scenario === 'AGRICULTURE_GREENHOUSE' ? s.crop : undefined,
      roomType: s.scenario === 'SMART_AIR_CONDITIONER' ? s.roomType : undefined,
      customTempMin: s.scenario === 'CUSTOM_SCENARIO' ? s.customTempMin : undefined,
      customTempMax: s.scenario === 'CUSTOM_SCENARIO' ? s.customTempMax : undefined,
      customHumidityMin: s.scenario === 'CUSTOM_SCENARIO' ? s.customHumidityMin : undefined,
      customHumidityMax: s.scenario === 'CUSTOM_SCENARIO' ? s.customHumidityMax : undefined,
      customGasMax: s.scenario === 'CUSTOM_SCENARIO' ? s.customGasMax : undefined,
    })
    result.value = res.data
    scenarioStore.setAnalysis(res.data)
  } catch (e: any) {
    error.value = e.response?.data?.message || '分析失败，请检查后端服务'
  } finally {
    loading.value = false
  }
}

function askAgent() {
  router.push('/agent')
  setTimeout(() => {
    window.dispatchEvent(new CustomEvent('agent-quick-prompt', {
      detail: `请基于当前${result.value?.scenarioName || '场景'}分析环境，并给出建议。`
    }))
  }, 300)
}

// Auto-run on first load if we have a cached result
watch(() => scenarioStore.lastAnalysis, (v) => {
  if (v && !result.value) result.value = v
}, { immediate: true })
</script>

<style scoped>
.role-grid,
.scenario-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 8px;
}

.select-card {
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.72), rgba(15, 23, 42, 0.62));
  backdrop-filter: blur(14px);
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 16px;
  padding: 18px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}
.select-card:hover {
  border-color: rgba(56, 189, 248, 0.35);
  transform: translateY(-1px);
}
.select-card.active {
  border-color: rgba(56, 189, 248, 0.55);
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.12), inset 0 1px 0 rgba(255,255,255,0.06);
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.18), rgba(15, 23, 42, 0.72));
}
.select-icon { font-size: 28px; margin-bottom: 8px; }
.select-label { font-size: 14px; font-weight: 600; color: var(--text-heading); margin-bottom: 4px; }
.select-desc { font-size: 11px; color: var(--text-dim); line-height: 1.4; }

.config-panel { padding: 20px 24px; }
.config-group { margin-bottom: 16px; }
.config-group label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.8px;
}
.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(148,163,184,0.18);
  color: var(--text-muted);
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.chip:hover { border-color: rgba(56,189,248,0.35); color: var(--text-primary); }
.chip.active {
  background: rgba(56,189,248,0.12);
  border-color: rgba(56,189,248,0.45);
  color: var(--blue);
  font-weight: 600;
}
.input-row { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; }
.input-group { display: flex; flex-direction: column; gap: 6px; }
.input-group span { font-size: 11px; color: var(--text-dim); }
.input-group input {
  background: rgba(2,6,23,0.6);
  border: 1px solid rgba(148,163,184,0.15);
  border-radius: 8px;
  padding: 8px 12px;
  color: var(--text-primary);
  font-size: 13px;
  outline: none;
}
.input-group input:focus { border-color: rgba(56,189,248,0.4); }
.config-actions { margin-top: 16px; display: flex; justify-content: flex-end; }

.btn-primary {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  border: none;
  color: #fff;
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.btn-primary:hover:not(:disabled) { box-shadow: 0 4px 16px rgba(37,99,235,0.35); transform: translateY(-1px); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.score-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 900px) {
  .score-grid { grid-template-columns: 1fr; }
  .role-grid, .scenario-grid { grid-template-columns: repeat(2, 1fr); }
}

.score-main { padding: 28px; text-align: center; }
.score-big {
  font-size: 80px;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(180deg, var(--text-heading), var(--text-muted));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 12px;
}
.score-excellent { background: linear-gradient(180deg, #4ade80, #22c55e); -webkit-background-clip: text; }
.score-good { background: linear-gradient(180deg, #38bdf8, #2563eb); -webkit-background-clip: text; }
.score-normal { background: linear-gradient(180deg, #fbbf24, #f59e0b); -webkit-background-clip: text; }
.score-warning { background: linear-gradient(180deg, #fca5a5, #ef4444); -webkit-background-clip: text; }
.score-level { margin-bottom: 12px; }
.score-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 12px;
  color: var(--text-dim);
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.score-summary { font-size: 14px; color: var(--text-muted); line-height: 1.6; }

.score-breakdown { padding: 24px; }
.score-breakdown h4 { font-size: 15px; color: var(--text-heading); margin-bottom: 16px; }
.breakdown-list { display: flex; flex-direction: column; gap: 14px; }
.breakdown-item { display: grid; grid-template-columns: 80px 1fr 36px; align-items: center; gap: 10px; }
.breakdown-item > span:first-child { font-size: 12px; color: var(--text-dim); }
.breakdown-item > span:last-child { font-size: 13px; font-weight: 600; color: var(--text-heading); text-align: right; }
.breakdown-bar { height: 8px; background: rgba(255,255,255,0.06); border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.6s ease; }

.metric-status-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
@media (max-width: 900px) {
  .metric-status-grid { grid-template-columns: repeat(2, 1fr); }
}
.metric-status-item { padding: 18px; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.ms-label { font-size: 12px; color: var(--text-dim); }

.risk-grid,
.advice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}
.risk-card { padding: 18px; }
.risk-header { margin-bottom: 10px; }
.risk-message { font-size: 13px; color: var(--text-muted); line-height: 1.6; }
.advice-card { padding: 18px; border-left: 3px solid var(--blue); }
.advice-title { font-size: 14px; font-weight: 600; color: var(--text-heading); margin-bottom: 6px; }
.advice-content { font-size: 13px; color: var(--text-muted); line-height: 1.6; }

.algo-card { padding: 20px 24px; }
.algo-list { margin: 0; padding-left: 18px; }
.algo-list li { font-size: 13px; color: var(--text-muted); line-height: 1.8; }

.agent-cta {
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 8px;
}
.cta-text { font-size: 14px; color: var(--text-muted); }

.spin { display: inline-block; animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>