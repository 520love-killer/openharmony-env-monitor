<template>
  <div class="page-shell">
    <PageHeader title="系统状态" subtitle="数据库、串口、缓存和数据清理策略" />

    <div class="system-grid">
      <BaseCard class="system-card">
        <h3>数据库状态</h3>
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
        <h3>串口实时接入</h3>
        <dl>
          <div><dt>接入状态</dt><dd>{{ serial?.connected ? '已连接' : '未连接' }}</dd></div>
          <div><dt>串口号</dt><dd>{{ serial?.portName || '-' }}</dd></div>
          <div><dt>波特率</dt><dd>{{ serial?.baudRate ?? '-' }}</dd></div>
          <div><dt>已接收行数</dt><dd>{{ serial?.receivedLines ?? '-' }}</dd></div>
          <div><dt>已保存记录</dt><dd>{{ serial?.savedRecords ?? '-' }}</dd></div>
        </dl>
      </BaseCard>

      <BaseCard class="system-card">
        <h3>缓存状态</h3>
        <dl>
          <div><dt>缓存技术栈</dt><dd>{{ cache?.provider || '-' }}</dd></div>
          <div><dt>缓存状态</dt><dd>{{ cache?.enabled ? '已启用' : '未启用' }}</dd></div>
          <div><dt>缓存 TTL</dt><dd>{{ cache?.ttlSeconds ?? '-' }} 秒</dd></div>
          <div><dt>最大数量</dt><dd>{{ cache?.maxSize ?? '-' }}</dd></div>
        </dl>
        <div class="tool-tags">
          <span v-for="n in (cache?.cacheNames || [])" :key="n">{{ n }}</span>
        </div>
      </BaseCard>

      <BaseCard class="system-card">
        <h3>数据清理策略</h3>
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
.system-grid { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); }
.system-card { padding: 16px; }
.system-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; color: #0f172a; }
dl { margin: 0; }
dl div { display: flex; justify-content: space-between; gap: 14px; padding: 8px 0; border-bottom: 1px solid #eef3f9; }
dl div:last-child { border-bottom: 0; }
dt { color: #64748b; font-size: 13px; }
dd { margin: 0; text-align: right; font-weight: 700; overflow-wrap: anywhere; font-size: 13px; }
.tool-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.tool-tags span {
  display: inline-flex; align-items: center; min-height: 24px;
  border-radius: 999px; padding: 3px 9px; font-size: 12px;
  font-weight: 600; color: #2563eb; background: #eff6ff; white-space: nowrap;
}
</style>
