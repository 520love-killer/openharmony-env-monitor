<template>
  <div class="page-shell">
    <PageHeader title="系统状态" subtitle="核心模块运行状态监控" />

    <div class="system-grid">
      <BaseCard class="system-card">
        <div class="card-top-line" />
        <div class="card-head">
          <h3>🗄 数据库状态</h3>
          <span class="status-light" :class="db?.connected ? 'on' : 'off'" />
        </div>
        <dl>
          <div><dt>MySQL 连接</dt><dd>{{ db?.connected ? '已连接' : '未确认' }}</dd></div>
          <div><dt>数据库名</dt><dd>{{ db?.database || '-' }}</dd></div>
          <div><dt>sensor_data 总数</dt><dd>{{ db?.sensorDataCount ?? '-' }}</dd></div>
          <div><dt>真实串口数量</dt><dd>{{ db?.realSerialCount ?? '-' }}</dd></div>
          <div><dt>真实 MQTT 数量</dt><dd>{{ db?.realMqttCount ?? '-' }}</dd></div>
          <div><dt>模拟演示数量</dt><dd>{{ db?.mockCount ?? '-' }}</dd></div>
        </dl>
      </BaseCard>

      <BaseCard class="system-card">
        <div class="card-top-line" />
        <div class="card-head">
          <h3>🔌 串口实时接入</h3>
          <span class="status-light" :class="serial?.connected ? 'on' : 'off'" />
        </div>
        <dl>
          <div><dt>接入状态</dt><dd>{{ serial?.connected ? '已连接' : '未连接' }}</dd></div>
          <div><dt>串口号</dt><dd>{{ serial?.portName || '-' }}</dd></div>
          <div><dt>波特率</dt><dd>{{ serial?.baudRate ?? '-' }}</dd></div>
          <div><dt>已接收行数</dt><dd>{{ serial?.receivedLines ?? '-' }}</dd></div>
          <div><dt>已保存记录</dt><dd>{{ serial?.savedRecords ?? '-' }}</dd></div>
        </dl>
      </BaseCard>

      <BaseCard class="system-card">
        <div class="card-top-line" />
        <div class="card-head">
          <h3>⚡ 缓存状态</h3>
          <span class="status-light" :class="cache?.enabled ? 'on' : 'off'" />
        </div>
        <dl>
          <div><dt>缓存技术栈</dt><dd>{{ cache?.provider || '-' }}</dd></div>
          <div><dt>缓存状态</dt><dd>{{ cache?.enabled ? '已启用' : '未启用' }}</dd></div>
          <div><dt>缓存 TTL</dt><dd>{{ cache?.ttlSeconds ?? '-' }} 秒</dd></div>
          <div><dt>最大数量</dt><dd>{{ cache?.maxSize ?? '-' }}</dd></div>
        </dl>
        <div class="cache-tags" v-if="cache?.cacheNames?.length">
          <span v-for="n in cache.cacheNames" :key="n">{{ n }}</span>
        </div>
      </BaseCard>

      <BaseCard class="system-card">
        <div class="card-top-line" />
        <div class="card-head">
          <h3>🧹 数据清理策略</h3>
          <span class="status-light on" />
        </div>
        <dl>
          <div><dt>真实原始数据</dt><dd>{{ retention?.rawDataRetentionDays ?? '-' }} 天</dd></div>
          <div><dt>模拟演示数据</dt><dd>{{ retention?.mockDataRetentionDays ?? '-' }} 天</dd></div>
          <div><dt>统计摘要</dt><dd>{{ retention?.summaryRetentionDays ?? '-' }} 天</dd></div>
          <div><dt>异常记录</dt><dd>{{ retention?.anomalyRetentionDays ?? '-' }} 天</dd></div>
          <div><dt>清理时间</dt><dd>{{ retention?.cleanupCron || '-' }}</dd></div>
        </dl>
      </BaseCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import { useSystemStore } from '@/stores/system'

const systemStore = useSystemStore()
const { db, serial, cache, retention } = systemStore

onMounted(() => systemStore.loadAll())
</script>

<style scoped>
.system-grid { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); }
.system-card { padding: 0; overflow: hidden; position: relative; }
.card-top-line {
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(56,189,248,0.30), transparent);
}
.card-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px 0;
}
.card-head h3 { font-size: 15px; font-weight: 600; color: var(--text-heading); }
.status-light {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.status-light.on { background: var(--green); box-shadow: 0 0 10px rgba(34, 197, 94, 0.5); }
.status-light.off { background: var(--text-dim); }

dl { margin: 0; padding: 14px 20px 18px; }
dl div { display: flex; justify-content: space-between; gap: 14px; padding: 8px 0; border-bottom: 1px solid rgba(148, 163, 184, 0.08); }
dl div:last-child { border-bottom: 0; }
dt { color: var(--text-dim); font-size: 12px; }
dd { margin: 0; text-align: right; font-weight: 700; overflow-wrap: anywhere; font-size: 13px; color: var(--text-primary); }

.cache-tags { display: flex; flex-wrap: wrap; gap: 8px; padding: 0 20px 16px; }
.cache-tags span {
  display: inline-flex; align-items: center; min-height: 24px;
  border-radius: 999px; padding: 3px 10px; font-size: 11px;
  font-weight: 600; color: var(--blue);
  background: rgba(56, 189, 248, 0.10);
  border: 1px solid rgba(56, 189, 248, 0.18);
}
</style>
