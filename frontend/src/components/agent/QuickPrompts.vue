<template>
  <BaseCard class="quick-panel">
    <div class="panel-header">
      <h3>⚡ 快捷提问</h3>
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
  { text: '⚠ 为什么出现异常？', question: '为什么出现异常？请分析可能原因' },
  { text: '🔮 未来 10 分钟温度会怎样？', question: '未来 10 分钟温度会怎样？' },
  { text: '📝 生成实验报告摘要', question: '请生成实验报告摘要' },
  { text: '🧮 解释使用了哪些算法', question: '请解释本系统使用了哪些算法' },
  { text: '📡 系统现在有没有真实数据？', question: '系统现在有没有真实数据？' },
]
</script>

<style scoped>
.quick-panel { overflow: hidden; }
.panel-header { padding: 13px 18px; border-bottom: 1px solid #eef3f9; }
.panel-header h3 { font-size: 14px; font-weight: 600; color: #0f172a; }
.quick-prompts { display: flex; flex-wrap: wrap; gap: 6px; padding: 14px 16px; }
.quick-btn {
  padding: 6px 13px; border: 1px solid #e5edf7; border-radius: 999px;
  background: #fff; color: #1f2937; font-size: 12px; cursor: pointer;
  transition: all 0.15s; white-space: nowrap; font-weight: 500;
}
.quick-btn:hover { border-color: #2563eb; color: #2563eb; background: #eff6ff; }
</style>
