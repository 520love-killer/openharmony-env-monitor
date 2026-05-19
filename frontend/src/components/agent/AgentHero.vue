<template>
  <BaseCard class="agent-hero">
    <div class="hero-scan" />
    <div class="hero-glow-top" />
    <div class="hero-avatar">
      <div class="avatar-ring">
        <svg viewBox="0 0 120 160" width="80" height="106">
          <defs>
            <linearGradient id="hero-fg" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#38bdf8"/>
              <stop offset="55%" stop-color="#ef4444"/>
              <stop offset="100%" stop-color="#f59e0b"/>
            </linearGradient>
            <filter id="hero-glow-svg">
              <feGaussianBlur stdDeviation="3" result="blur"/>
              <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
          </defs>
          <rect x="42" y="20" width="36" height="100" rx="18" fill="url(#hero-fg)" filter="url(#hero-glow-svg)"/>
          <circle cx="60" cy="30" r="18" fill="#38bdf8"/>
          <circle cx="60" cy="120" r="22" fill="#f59e0b"/>
          <circle cx="52" cy="62" r="4" fill="#fff"/>
          <circle cx="68" cy="62" r="4" fill="#fff"/>
          <circle cx="52" cy="63" r="1.5" fill="#08111f"/>
          <circle cx="68" cy="63" r="1.5" fill="#08111f"/>
          <path d="M55 72 Q60 78 65 72" stroke="#fff" stroke-width="2" fill="none" stroke-linecap="round"/>
        </svg>
      </div>
    </div>
    <div class="hero-body">
      <h1>温度计 · 环境监测智能助手</h1>
      <p>基于 Hi3861 真实串口数据，DeepSeek Agent 驱动，实时分析温度变化、预测趋势、诊断异常风险，并生成实验报告。</p>
      <div class="hero-tags">
        <StatusBadge type="safe" dot>在线</StatusBadge>
        <StatusBadge type="serial">{{ source }}</StatusBadge>
        <StatusBadge type="deepseek">{{ model }}</StatusBadge>
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
  gap: 28px;
  padding: 28px 32px;
  position: relative;
  overflow: hidden;
  min-height: 150px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.13), rgba(139, 92, 246, 0.10));
}

.hero-glow-top {
  position: absolute;
  top: -40px; right: -40px;
  width: 200px; height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(56,189,248,0.10), transparent 70%);
  pointer-events: none;
}

.hero-scan {
  position: absolute;
  top: 0; left: -100%;
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(56, 189, 248, 0.04), transparent);
  animation: scanLine 4s ease-in-out infinite;
  pointer-events: none;
}
@keyframes scanLine {
  0% { left: -60%; }
  100% { left: 120%; }
}

.hero-avatar {
  flex-shrink: 0;
  width: 110px;
  display: flex; align-items: center; justify-content: center;
}
.avatar-ring {
  padding: 8px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(56,189,248,0.18), transparent 70%);
  box-shadow: 0 0 40px rgba(56, 189, 248, 0.12);
}

.hero-body { flex: 1; min-width: 0; }
.hero-body h1 {
  font-size: 24px; font-weight: 800; color: var(--text-heading); margin-bottom: 8px;
  letter-spacing: -0.3px;
  background: linear-gradient(135deg, #e5f0ff, #bae6fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-body > p {
  color: var(--text-muted); font-size: 13px; line-height: 1.7; margin-bottom: 14px; max-width: 520px;
}
.hero-tags { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
