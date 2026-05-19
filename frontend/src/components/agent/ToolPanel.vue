<template>
  <BaseCard class="tool-panel">
    <div class="panel-header">
      <h3>🔧 工具面板</h3>
    </div>
    <div class="tool-grid">
      <div
        v-for="tool in tools"
        :key="tool.name"
        class="tool-item"
        @click="emit('select', tool.question)"
      >
        <span class="tool-icon">{{ tool.icon }}</span>
        <div class="tool-body">
          <strong>{{ tool.label }}</strong>
          <p>{{ tool.desc }}</p>
        </div>
        <span class="tool-arrow">→</span>
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
  { name: 'getLatestSensorData', icon: '📡', label: '最新数据', desc: '获取最新传感器数据', question: '请获取最新传感器数据' },
  { name: 'getRecent50Data', icon: '📋', label: '最近 50 条', desc: '读取最近 50 条数据', question: '请查看最近50条温度数据，并按时间顺序列出来。' },
  { name: 'getAnalyticsSummary', icon: '📊', label: '统计分析', desc: '统计分析摘要', question: '请提供统计分析摘要' },
  { name: 'getTemperatureForecast', icon: '🔮', label: '温度预测', desc: '温度趋势预测', question: '请预测未来温度趋势' },
  { name: 'getAnomalyDetection', icon: '⚠', label: '异常检测', desc: '异常风险检测', question: '请检测是否有异常数据' },
  { name: 'getDatabaseStatus', icon: '🗄', label: '数据库状态', desc: '数据库状态检查', question: '请检查数据库状态' },
  { name: 'generateReportSummary', icon: '📝', label: '报告摘要', desc: '生成实验报告摘要', question: '请生成实验报告摘要' },
]
</script>

<style scoped>
.tool-panel { overflow: hidden; }
.panel-header { padding: 13px 18px; border-bottom: 1px solid #eef3f9; }
.panel-header h3 { font-size: 14px; font-weight: 600; color: #0f172a; }
.tool-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0; padding: 6px; }
.tool-item {
  display: flex; align-items: flex-start; gap: 8px; padding: 10px 8px;
  cursor: pointer; transition: all 0.2s; border-radius: 10px;
  border: 1.5px solid transparent; margin: 2px;
}
.tool-item:hover {
  background: #eff6ff; border-color: #2563eb;
  transform: translateY(-1px); box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.tool-icon {
  flex-shrink: 0; font-size: 16px; width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  background: #f3f4f6; border-radius: 7px; margin-top: 1px;
}
.tool-body { flex: 1; min-width: 0; }
.tool-body strong { font-size: 11px; color: #7c3aed; display: block; font-weight: 600; }
.tool-body p { font-size: 10px; color: #64748b; margin-top: 1px; line-height: 1.3; }
.tool-arrow {
  flex-shrink: 0; font-size: 12px; color: #64748b;
  transition: transform 0.2s; align-self: center;
}
.tool-item:hover .tool-arrow { transform: translateX(3px); color: #2563eb; }
</style>
