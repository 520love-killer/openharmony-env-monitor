<template>
  <BaseCard class="agent-hero">
    <div class="hero-avatar">
      <svg viewBox="0 0 120 160" width="90" height="120">
        <defs>
          <linearGradient id="fg" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#3b82f6"/>
            <stop offset="60%" stop-color="#ef4444"/>
            <stop offset="100%" stop-color="#fbbf24"/>
          </linearGradient>
        </defs>
        <rect x="42" y="20" width="36" height="100" rx="18" fill="url(#fg)"/>
        <circle cx="60" cy="30" r="18" fill="#3b82f6"/>
        <circle cx="60" cy="120" r="22" fill="#fbbf24"/>
        <circle cx="52" cy="62" r="4" fill="#fff"/>
        <circle cx="68" cy="62" r="4" fill="#fff"/>
        <circle cx="52" cy="63" r="1.5" fill="#172033"/>
        <circle cx="68" cy="63" r="1.5" fill="#172033"/>
        <path d="M55 72 Q60 78 65 72" stroke="#fff" stroke-width="2" fill="none" stroke-linecap="round"/>
      </svg>
    </div>
    <div class="hero-body">
      <h1>温度计 · 环境监测智能助手</h1>
      <p>读取 Hi3861 采集的环境数据，分析温度变化，预测趋势，解释异常风险，并帮助你生成实验报告。</p>
      <div class="hero-tags">
        <StatusBadge type="safe" dot>在线</StatusBadge>
        <StatusBadge type="serial">数据来源：{{ source }}</StatusBadge>
        <StatusBadge type="deepseek">模型：{{ model }}</StatusBadge>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '@/components/common/BaseCard.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

defineProps<{
  source: string
  model: string
}>()
</script>

<style scoped>
.agent-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #f0f7ff 0%, #f8fbff 50%, #f0f4ff 100%);
  min-height: 150px;
}
.hero-avatar {
  flex-shrink: 0;
  width: 100px;
  display: flex; align-items: center; justify-content: center;
}
.hero-body { flex: 1; min-width: 0; }
.hero-body h1 {
  font-size: 20px; font-weight: 700; color: #0f172a; margin-bottom: 6px;
}
.hero-body > p {
  color: #64748b; font-size: 13px; line-height: 1.6; margin-bottom: 12px; max-width: 480px;
}
.hero-tags { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
