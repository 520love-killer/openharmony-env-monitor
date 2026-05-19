<template>
  <BaseCard class="tool-panel">
    <div class="panel-header">
      <div class="panel-title-row">
        <span class="panel-icon">◈</span>
        <h3>工具矩阵</h3>
      </div>
      <span class="panel-subtitle">Tool Matrix</span>
    </div>
    <div class="tool-grid">
      <div
        v-for="tool in tools"
        :key="tool.name"
        class="tool-item"
        @click="emit('select', tool.question)"
      >
        <span class="tool-icon" :style="{ color: tool.color }">{{ tool.icon }}</span>
        <div class="tool-body">
          <strong>{{ tool.label }}</strong>
          <p>{{ tool.name }}</p>
        </div>
        <span class="tool-arrow">▸</span>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '@/components/common/BaseCard.vue'

const emit = defineEmits<{
  select: [string]
}>()

const tools = [
  { name: 'getLatestSensorData', icon: '📡', label: '最新数据', color: '#38bdf8', question: '请获取最新传感器数据' },
  { name: 'getRecent50Data', icon: '📋', label: '最近 50 条', color: '#22d3ee', question: '请查看最近50条温度数据，并按时间顺序列出来。' },
  { name: 'getAnalyticsSummary', icon: '📊', label: '统计分析', color: '#8b5cf6', question: '请提供统计分析摘要' },
  { name: 'getTemperatureForecast', icon: '🔮', label: '温度预测', color: '#f59e0b', question: '请预测未来温度趋势' },
  { name: 'getAnomalyDetection', icon: '⚠', label: '异常检测', color: '#ef4444', question: '请检测是否有异常数据' },
  { name: 'getDatabaseStatus', icon: '🗄', label: '数据库状态', color: '#22c55e', question: '请检查数据库状态' },
  { name: 'generateReportSummary', icon: '📝', label: '报告摘要', color: '#a78bfa', question: '请生成实验报告摘要' },
]
</script>

<style scoped>
.tool-panel { overflow: hidden; }
.panel-header {
  padding: 14px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}
.panel-title-row { display: flex; align-items: center; gap: 8px; }
.panel-icon { color: var(--blue); font-size: 12px; }
.panel-header h3 { font-size: 14px; font-weight: 700; color: var(--text-heading); }
.panel-subtitle { font-size: 10px; color: var(--text-dim); margin-top: 2px; display: block; letter-spacing: 1px; }

.tool-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 4px; padding: 8px; }
.tool-item {
  display: flex; align-items: center; gap: 8px; padding: 10px 8px;
  cursor: pointer; transition: all 0.2s; border-radius: 10px;
  border: 1.5px solid transparent; margin: 2px;
}
.tool-item:hover {
  background: rgba(56, 189, 248, 0.06);
  border-color: rgba(56, 189, 248, 0.28);
  transform: translateY(-1px);
}
.tool-icon {
  flex-shrink: 0; font-size: 18px; width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(148, 163, 184, 0.08); border-radius: 8px;
}
.tool-body { flex: 1; min-width: 0; }
.tool-body strong { font-size: 11px; color: var(--text-primary); display: block; font-weight: 600; }
.tool-body p {
  font-size: 9px; color: var(--text-dim); margin-top: 1px; line-height: 1.3;
  font-family: "Cascadia Code", "Fira Code", monospace;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.tool-arrow {
  flex-shrink: 0; font-size: 12px; color: var(--text-dim);
  transition: all 0.2s; opacity: 0;
}
.tool-item:hover .tool-arrow { opacity: 1; color: var(--blue); transform: translateX(2px); }
</style>
