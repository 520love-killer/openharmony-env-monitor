<template>
  <BaseCard class="rt-panel">
    <div class="panel-header">
      <div class="panel-title-row">
        <span class="panel-icon">◉</span>
        <h3>实时概览</h3>
      </div>
      <span class="live-badge">LIVE</span>
    </div>
    <div class="rt-grid">
      <div class="rt-item">
        <span class="rt-label">温度</span>
        <span class="rt-value" style="color: #f97316; text-shadow: 0 0 20px rgba(249, 115, 22, 0.3);">{{ temp }}</span>
        <svg class="rt-wave" viewBox="0 0 80 20" width="80" height="20">
          <path d="M0 10 Q10 5 20 10 Q30 15 40 10 Q50 5 60 10 Q70 15 80 10" fill="none" stroke="#f97316" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
      <div class="rt-item">
        <span class="rt-label">湿度</span>
        <span class="rt-value" style="color: #38bdf8; text-shadow: 0 0 20px rgba(56, 189, 248, 0.3);">{{ humidity }}</span>
        <svg class="rt-wave" viewBox="0 0 80 20" width="80" height="20">
          <path d="M0 10 Q10 15 20 10 Q30 5 40 10 Q50 15 60 10 Q70 5 80 10" fill="none" stroke="#38bdf8" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
      <div class="rt-item">
        <span class="rt-label">燃气</span>
        <span class="rt-value" style="color: #a78bfa; text-shadow: 0 0 20px rgba(139, 92, 246, 0.3);">{{ gas }}</span>
        <svg class="rt-wave" viewBox="0 0 80 20" width="80" height="20">
          <path d="M0 10 Q10 5 20 10 Q30 18 40 10 Q50 2 60 10 Q70 15 80 10" fill="none" stroke="#a78bfa" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
      <div class="rt-item rt-status" :class="statusClass">
        <span class="rt-label">状态</span>
        <span class="rt-value">
          <span class="status-dot-inline" />
          {{ status }}
        </span>
        <div style="height:20px;" />
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
.panel-header {
  padding: 14px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  display: flex; align-items: center; justify-content: space-between;
}
.panel-title-row { display: flex; align-items: center; gap: 8px; }
.panel-icon { color: var(--green); font-size: 12px; }
.panel-header h3 { font-size: 14px; font-weight: 700; color: var(--text-heading); }
.live-badge {
  font-size: 9px; font-weight: 700; color: var(--green);
  background: rgba(34, 197, 94, 0.12);
  padding: 2px 8px; border-radius: 999px;
  border: 1px solid rgba(34, 197, 94, 0.25);
  letter-spacing: 1px;
  animation: breath 2s ease-in-out infinite;
}
@keyframes breath {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.rt-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; padding: 14px 16px; }
.rt-item {
  background: rgba(2, 6, 23, 0.45);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 14px;
  padding: 16px 12px 12px;
  text-align: center;
  transition: all 0.2s;
}
.rt-item:hover { border-color: rgba(56, 189, 248, 0.25); }
.rt-label { display: block; font-size: 10px; color: var(--text-dim); margin-bottom: 4px; font-weight: 500; letter-spacing: 0.5px; text-transform: uppercase; }
.rt-value { display: block; font-size: 28px; font-weight: 800; line-height: 1.15; margin-bottom: 6px; }
.rt-wave { display: block; margin: 0 auto; opacity: 0.3; }
.rt-footer { padding: 10px 16px; font-size: 10px; color: var(--text-dim); border-top: 1px solid rgba(148, 163, 184, 0.10); text-align: center; }

.rt-status.safe-text .status-dot-inline {
  width: 8px; height: 8px; border-radius: 50%; display: inline-block;
  background: var(--green);
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.5);
  animation: breath 2s ease-in-out infinite;
  margin-right: 4px; vertical-align: middle;
}
.rt-status.warning-text .status-dot-inline {
  width: 8px; height: 8px; border-radius: 50%; display: inline-block;
  background: var(--red);
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
  margin-right: 4px; vertical-align: middle;
}
.status-dot-inline { margin-right: 4px; vertical-align: middle; }
</style>
