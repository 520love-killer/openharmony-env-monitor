<template>
  <BaseCard class="quick-panel">
    <div class="panel-header">
      <div class="panel-title-row">
        <span class="panel-icon">⚡</span>
        <h3>快捷提问</h3>
      </div>
    </div>
    <div class="quick-prompts">
      <button
        v-for="p in prompts"
        :key="p.text"
        class="quick-btn"
        @click="emit('select', p.question)"
      >
        {{ p.text }}
      </button>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '@/components/common/BaseCard.vue'

const emit = defineEmits<{
  select: [string]
}>()

const prompts = [
  { text: '📊 分析当前环境', question: '请分析当前环境' },
  { text: '📋 查看最近温度', question: '请查看最近50条温度数据，并按时间顺序列出来。' },
  { text: '📈 分析温度趋势', question: '请分析温度变化趋势' },
  { text: '⚠ 为什么会异常？', question: '为什么出现异常？请分析可能原因' },
  { text: '🔮 预测温度变化', question: '未来 10 分钟温度会怎样？' },
  { text: '📝 生成报告摘要', question: '请生成实验报告摘要' },
  { text: '🧮 解释算法原理', question: '请解释本系统使用了哪些算法' },
  { text: '📡 检查真实数据', question: '系统现在有没有真实数据？' },
]
</script>

<style scoped>
.quick-panel { overflow: hidden; }
.panel-header { padding: 14px 18px; border-bottom: 1px solid rgba(148, 163, 184, 0.12); }
.panel-title-row { display: flex; align-items: center; gap: 8px; }
.panel-icon { font-size: 14px; }
.panel-header h3 { font-size: 14px; font-weight: 700; color: var(--text-heading); }
.quick-prompts { display: flex; flex-wrap: wrap; gap: 6px; padding: 14px 16px; }
.quick-btn {
  padding: 6px 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.06);
  color: var(--text-muted);
  font-size: 12px; cursor: pointer;
  transition: all 0.2s; white-space: nowrap; font-weight: 500;
}
.quick-btn:hover {
  border-color: var(--blue);
  color: var(--blue);
  background: rgba(56, 189, 248, 0.08);
  box-shadow: 0 0 16px rgba(56, 189, 248, 0.10);
}
</style>
