<template>
  <div class="page-shell">
    <PageHeader title="关于项目" subtitle="OpenHarmony Hi3861 环境监测预警系统 v2.0" />

    <BaseCard class="about-hero">
      <div class="about-hero-icon">
        <svg viewBox="0 0 48 64" width="64" height="85">
          <defs>
            <linearGradient id="about-fg" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#38bdf8"/>
              <stop offset="55%" stop-color="#ef4444"/>
              <stop offset="100%" stop-color="#f59e0b"/>
            </linearGradient>
          </defs>
          <rect x="16" y="6" width="16" height="42" rx="8" fill="url(#about-fg)"/>
          <circle cx="24" cy="12" r="8" fill="#38bdf8"/>
          <circle cx="24" cy="50" r="10" fill="#f59e0b"/>
        </svg>
      </div>
      <div>
        <h3>OpenHarmony Hi3861 环境监测预警系统 v2.0</h3>
        <p>基于真实串口数据、Spring Boot、MySQL、Vue3、ECharts 与 DeepSeek Agent 的智能环境监测平台。硬件端采集温度、湿度、燃气数据，通过串口实时传输到 Spring Boot 后端，存储到 MySQL，Web Dashboard 提供数据可视化、统计分析、温度预测和异常检测。</p>
      </div>
    </BaseCard>

    <SectionHeader title="系统架构" />
    <BaseCard class="arch-card">
      <div class="arch-flow">
        <div v-for="(node, i) in arch" :key="i" class="arch-node-wrap">
          <div class="arch-node" :class="`arch-${node.type}`">
            <div class="arch-node-label" v-html="node.label" />
            <span v-if="node.sub" class="arch-node-sub">{{ node.sub }}</span>
          </div>
          <div v-if="i < arch.length - 1" class="arch-arrow">↓</div>
        </div>
      </div>
    </BaseCard>

    <SectionHeader title="v2.0 核心能力" />
    <div class="about-grid">
      <BaseCard v-for="c in cards" :key="c.title" class="about-card">
        <div class="about-card-icon">{{ c.icon }}</div>
        <h4>{{ c.title }}</h4>
        <p v-html="c.content" />
      </BaseCard>
    </div>

    <SectionHeader title="技术栈" />
    <BaseCard class="tech-stack-card">
      <div class="tech-tags">
        <span v-for="t in techStack" :key="t" class="tech-tag">{{ t }}</span>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'

const cards = [
  {
    icon: '📡', title: '真实数据接入',
    content: 'Hi3861 开发板通过串口实时传输 AHT20 + MQ-2 传感器数据到后端，支持 REAL_SERIAL 和 REAL_MQTT 双模式。',
  },
  {
    icon: '📊', title: '智能统计分析',
    content: '基于最近 50 条数据计算平均值、标准差、变化率、波动程度等统计指标，自动判断数据趋势。',
  },
  {
    icon: '🔮', title: '温度趋势预测',
    content: '使用<strong>滑动平均、线性回归、指数平滑</strong>三种轻量算法综合预测未来温度变化趋势。',
  },
  {
    icon: '⚠', title: '异常检测诊断',
    content: '支持<strong>阈值超限、突变检测、连续升高、长期无变化</strong>四类异常检测规则。',
  },
  {
    icon: '🤖', title: 'DeepSeek Agent',
    content: '集成 DeepSeek 大模型，支持<strong>流式输出、多轮对话、上下文记忆</strong>，自动调用工具获取数据。',
  },
  {
    icon: '🎨', title: 'Vue3 科技风前端',
    content: '轻暗黑科技风设计、ECharts 数据可视化、响应式布局、毛玻璃卡片，适合项目展示与答辩。',
  },
]

const arch = [
  { label: 'Hi3861', sub: 'AHT20 + MQ-2', type: 'hardware' },
  { label: '串口 / MQTT', sub: 'UART 通信', type: 'com' },
  { label: 'Spring Boot', sub: 'Java 21', type: 'backend' },
  { label: 'MySQL', sub: 'Caffeine 缓存', type: 'db' },
  { label: 'DeepSeek', sub: '流式 Agent', type: 'ai' },
  { label: 'Vue3 Dashboard', sub: 'ECharts 可视化', type: 'web' },
]

const techStack = [
  'OpenHarmony', 'Hi3861', 'AHT20', 'MQ-2',
  'Java 21', 'Spring Boot', 'MySQL', 'Caffeine Cache',
  'Vue3', 'TypeScript', 'Vite', 'Pinia', 'Vue Router',
  'ECharts', 'vue-echarts', 'Axios', 'markdown',
  'DeepSeek API', 'SSE 流式输出', 'localStorage 记忆',
]
</script>

<style scoped>
.about-hero {
  display: flex; align-items: center; gap: 28px;
  padding: 32px 36px; margin-bottom: 24px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.10), rgba(139, 92, 246, 0.08));
}
.about-hero-icon { flex-shrink: 0; filter: drop-shadow(0 0 16px rgba(56,189,248,0.3)); }
.about-hero h3 { font-size: 22px; font-weight: 700; color: var(--text-heading); margin-bottom: 8px; }
.about-hero p { font-size: 14px; color: var(--text-muted); line-height: 1.7; }

.arch-card { padding: 28px 24px; margin-bottom: 24px; }
.arch-flow { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; justify-content: center; }
.arch-node-wrap { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.arch-node {
  border-radius: 16px; padding: 16px 22px; text-align: center;
  font-size: 14px; font-weight: 700; min-width: 110px;
  border: 1.5px solid rgba(148, 163, 184, 0.14);
}
.arch-node-label { color: var(--text-primary); }
.arch-node-sub { display: block; font-size: 10px; font-weight: 400; margin-top: 4px; color: var(--text-dim); }
.arch-arrow { font-size: 20px; color: var(--blue); }
.arch-hardware { border-color: rgba(249,115,22,0.35); background: rgba(249,115,22,0.08); }
.arch-com { border-color: rgba(139,92,246,0.35); background: rgba(139,92,246,0.08); }
.arch-backend { border-color: rgba(56,189,248,0.35); background: rgba(56,189,248,0.08); }
.arch-db { border-color: rgba(34,197,94,0.35); background: rgba(34,197,94,0.08); }
.arch-ai { border-color: rgba(139,92,246,0.40); background: rgba(139,92,246,0.12); box-shadow: 0 0 20px rgba(139,92,246,0.10); }
.arch-web { border-color: rgba(34,211,238,0.35); background: rgba(34,211,238,0.08); }

.about-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 16px; margin-bottom: 20px; }
.about-card { padding: 22px 24px; }
.about-card-icon { font-size: 28px; margin-bottom: 10px; }
.about-card h4 { font-size: 16px; font-weight: 600; color: var(--text-heading); margin-bottom: 8px; }
.about-card p { font-size: 13px; color: var(--text-muted); line-height: 1.7; }
.about-card p :deep(strong) { color: var(--blue); }

.tech-stack-card { padding: 24px 28px; }
.tech-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tech-tag {
  display: inline-flex; align-items: center; min-height: 28px;
  border-radius: 999px; padding: 4px 14px;
  font-size: 12px; font-weight: 600;
  color: var(--blue);
  background: rgba(56, 189, 248, 0.08);
  border: 1px solid rgba(56, 189, 248, 0.18);
  transition: all 0.2s;
}
.tech-tag:hover {
  background: rgba(56, 189, 248, 0.14);
  border-color: rgba(56, 189, 248, 0.35);
}
</style>
