<template>
  <div class="page-shell">
    <PageHeader title="关于项目" subtitle="OpenHarmony Hi3861 环境监测预警系统 v2.0" />

    <BaseCard class="about-hero">
      <div class="about-hero-icon">🌡</div>
      <div>
        <h3>OpenHarmony Hi3861 环境监测预警系统 v2.0</h3>
        <p>基于真实串口数据、Spring Boot、MySQL、DeepSeek Agent 的智能环境监测平台。</p>
      </div>
    </BaseCard>

    <div class="about-grid">
      <BaseCard v-for="c in cards" :key="c.title" class="about-card">
        <h4>{{ c.title }}</h4>
        <p v-html="c.content" />
      </BaseCard>
    </div>

    <BaseCard class="arch-card">
      <h4>系统架构</h4>
      <div class="arch-flow">
        <div v-for="(node, i) in arch" :key="i" class="arch-node" :class="`arch-${node.type}`">
          <div v-html="node.label" />
          <span v-if="node.sub">{{ node.sub }}</span>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'

const cards = [
  {
    title: '项目简介',
    content: '基于 OpenHarmony Hi3861 的环境监测预警系统，硬件端采集温度、湿度、燃气数据，通过串口或 MQTT 实时传输到 Spring Boot 后端，存储到 MySQL 数据库，Web Dashboard 提供数据可视化、统计分析、温度预测和异常检测功能。',
  },
  {
    title: 'v2.0 核心变化',
    content: 'v2.0 将系统从传统 Dashboard 升级为以 <strong>温度计 Agent</strong> 为核心的智能环境分析平台。支持 DeepSeek 大模型流式输出、多轮对话上下文记忆、RAG 知识库检索。',
  },
  {
    title: '数据真实性',
    content: '页面默认读取 REAL_SERIAL 真实串口数据；如果切换到 MOCK，页面和 Agent 回答都会明确标记为模拟演示数据。',
  },
  {
    title: '算法说明',
    content: '使用滑动平均、线性回归、指数平滑进行短期温度预测，结合阈值检测、突变检测、连续升高检测进行异常诊断。适合趋势判断和短期预警，不是工业级精确温控模型。',
  },
]

const arch = [
  { label: 'Hi3861', sub: 'AHT20 + MQ-2', type: 'hardware' },
  { label: '串口 / MQTT', sub: '', type: 'com' },
  { label: 'Spring Boot', sub: 'Java 21', type: 'backend' },
  { label: 'MySQL', sub: 'Caffeine Cache', type: 'db' },
  { label: 'DeepSeek Agent', sub: '流式输出', type: 'ai' },
  { label: 'Vue3 Web Dashboard', sub: '数据可视化', type: 'web' },
]
</script>

<style scoped>
.about-hero {
  display: flex; align-items: center; gap: 20px;
  padding: 28px 32px; margin-bottom: 20px;
  background: linear-gradient(135deg, #f0f7ff, #f8fbff);
}
.about-hero-icon { font-size: 48px; flex-shrink: 0; }
.about-hero h3 { font-size: 20px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }
.about-hero p { font-size: 14px; color: #64748b; line-height: 1.6; }

.about-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 14px; margin-bottom: 20px; }
.about-card { padding: 20px 22px; }
.about-card h4 { font-size: 16px; font-weight: 600; color: #0f172a; margin-bottom: 8px; }
.about-card p { font-size: 13px; color: #64748b; line-height: 1.7; }
.about-card p :deep(strong) { color: #2563eb; }

.arch-card { padding: 24px 28px; }
.arch-card h4 { font-size: 16px; font-weight: 600; margin-bottom: 18px; color: #0f172a; }
.arch-flow { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; justify-content: center; }
.arch-node {
  background: #f8fbff; border: 1.5px solid #e5edf7; border-radius: 12px;
  padding: 12px 18px; text-align: center; font-size: 13px; font-weight: 600; color: #0f172a;
  min-width: 100px; box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.arch-node span { display: block; font-size: 10px; font-weight: 400; color: #64748b; margin-top: 3px; }
.arch-hardware { border-color: #f97316; background: #fff7ed; }
.arch-com { border-color: #8b5cf6; background: #f5f3ff; }
.arch-backend { border-color: #2563eb; background: #eff6ff; }
.arch-db { border-color: #10b981; background: #ecfdf5; }
.arch-ai { border-color: #7c3aed; background: #f5f3ff; }
.arch-web { border-color: #0ea5e9; background: #f0f9ff; }
</style>
