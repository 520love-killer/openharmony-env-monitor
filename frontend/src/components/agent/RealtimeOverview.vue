<template>
  <BaseCard class="rt-panel">
    <div class="panel-header">
      <h3>📡 实时概览</h3>
    </div>
    <div class="rt-grid">
      <div class="rt-item">
        <span class="rt-label">当前温度</span>
        <span class="rt-value" :style="{ color: '#ef4444' }">{{ temp }}</span>
        <svg class="rt-wave" viewBox="0 0 80 20" width="80" height="20">
          <path d="M0 10 Q10 5 20 10 Q30 15 40 10 Q50 5 60 10 Q70 15 80 10" fill="none" stroke="#ef4444" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
      <div class="rt-item">
        <span class="rt-label">湿度</span>
        <span class="rt-value" :style="{ color: '#0ea5e9' }">{{ humidity }}</span>
        <svg class="rt-wave" viewBox="0 0 80 20" width="80" height="20">
          <path d="M0 10 Q10 15 20 10 Q30 5 40 10 Q50 15 60 10 Q70 5 80 10" fill="none" stroke="#0ea5e9" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
      <div class="rt-item">
        <span class="rt-label">燃气浓度</span>
        <span class="rt-value" :style="{ color: '#8b5cf6' }">{{ gas }}</span>
        <svg class="rt-wave" viewBox="0 0 80 20" width="80" height="20">
          <path d="M0 10 Q10 5 20 10 Q30 18 40 10 Q50 2 60 10 Q70 15 80 10" fill="none" stroke="#8b5cf6" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
      <div class="rt-item">
        <span class="rt-label">状态</span>
        <span class="rt-value" :class="statusClass">{{ status }}</span>
        <div class="rt-wave" style="height:20px;" />
      </div>
    </div>
    <div class="rt-footer">{{ footer }}</div>
  </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '@/components/common/BaseCard.vue'

defineProps<{
  temp: string
  humidity: string
  gas: string
  status: string
  statusClass?: string
  footer: string
}>()
</script>

<style scoped>
.rt-panel { overflow: hidden; }
.panel-header { padding: 13px 18px; border-bottom: 1px solid #eef3f9; }
.panel-header h3 { font-size: 14px; font-weight: 600; color: #0f172a; }
.rt-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; padding: 12px 16px; }
.rt-item {
  background: #f8fbff; border: 1px solid #eef3f9; border-radius: 12px;
  padding: 14px 12px 10px; text-align: center; transition: all 0.15s;
}
.rt-item:hover { border-color: #e5edf7; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.rt-label { display: block; font-size: 11px; color: #64748b; margin-bottom: 4px; font-weight: 500; }
.rt-value { display: block; font-size: 22px; font-weight: 700; color: #0f172a; margin-bottom: 6px; line-height: 1.2; }
.rt-wave { display: block; margin: 0 auto; opacity: 0.4; }
.rt-footer { padding: 10px 16px; font-size: 11px; color: #64748b; border-top: 1px solid #eef3f9; text-align: center; }
</style>
