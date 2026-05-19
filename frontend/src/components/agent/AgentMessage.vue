<template>
  <div class="chat-message" :class="`chat-${role}`">
    <div class="chat-bubble-wrap">
      <div v-if="role === 'assistant'" class="chat-avatar chat-avatar-agent">🌡</div>
      <div class="chat-bubble" :class="{ streaming: isStreaming }" v-html="htmlContent" />
      <div v-if="role === 'user'" class="chat-avatar chat-avatar-user">👤</div>
    </div>
    <div v-if="time" class="chat-time">{{ time }}</div>
    <div v-if="tools && tools.length" class="chat-tools">
      <span v-for="t in tools" :key="t" class="tool-tag">{{ toolLabel(t) }}</span>
    </div>
    <div v-if="dataSource && role === 'assistant'" class="chat-meta">
      数据来源：{{ dataSource }}{{ confidence ? ` · 置信度：${confidence}` : '' }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps<{
  role: 'user' | 'assistant'
  content: string
  time?: string
  tools?: string[]
  dataSource?: string
  confidence?: string
  isStreaming?: boolean
}>()

const toolMap: Record<string, string> = {
  getLatestSensorData: '最新数据',
  getRecent50Data: '最近 50 条',
  getAnalyticsSummary: '统计分析',
  getTemperatureForecast: '温度预测',
  getAnomalyDetection: '异常检测',
  getDatabaseStatus: '数据库状态',
  generateReportSummary: '报告摘要',
}

function toolLabel(name: string) {
  return toolMap[name] || name
}

const htmlContent = computed(() => {
  if (!props.content) return ''
  if (props.isStreaming) {
    return escHtml(props.content)
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/### (.+)/g, '<h3>$1</h3>')
      .replace(/## (.+)/g, '<h2>$1</h2>')
      .replace(/# (.+)/g, '<h1>$1</h1>')
      .replace(/\n/g, '<br>')
      .replace(/-{3,}/g, '<hr>')
  }
  const raw = marked.parse(props.content, { async: false }) as string
  return raw
})

function escHtml(v: string) {
  return v.replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!))
}
</script>

<style scoped>
.chat-message { margin-bottom: 22px; display: flex; flex-direction: column; }
.chat-message.chat-user { align-items: flex-end; }
.chat-message.chat-agent { align-items: flex-start; }

.chat-bubble-wrap { display: flex; gap: 10px; max-width: 75%; }
.chat-user .chat-bubble-wrap { flex-direction: row-reverse; max-width: 60%; }

.chat-avatar {
  flex-shrink: 0; width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; font-size: 16px; margin-top: 2px;
}
.chat-avatar-agent {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; font-size: 14px;
  box-shadow: 0 0 12px rgba(139, 92, 246, 0.3);
}
.chat-avatar-user {
  background: rgba(148, 163, 184, 0.18);
}

.chat-bubble {
  padding: 12px 16px; border-radius: 16px; line-height: 1.7; font-size: 14px; word-break: break-word;
}
.chat-user .chat-bubble {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  border-bottom-right-radius: 6px;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);
}
.chat-agent .chat-bubble {
  background: rgba(15, 23, 42, 0.78);
  border: 1px solid rgba(56, 189, 248, 0.15);
  border-bottom-left-radius: 6px;
  color: var(--text-primary);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
.chat-bubble.streaming {
  border-color: rgba(56, 189, 248, 0.35);
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.06);
}

.chat-time { font-size: 11px; color: var(--text-dim); margin-top: 6px; padding: 0 14px; }

.chat-tools { display: flex; flex-wrap: wrap; gap: 5px; margin-top: 8px; padding: 0 14px; }
.tool-tag {
  font-size: 10px; color: var(--blue); background: rgba(56, 189, 248, 0.10);
  padding: 3px 9px; border-radius: 999px; font-weight: 600;
  border: 1px solid rgba(56, 189, 248, 0.18);
}

.chat-meta { font-size: 11px; color: var(--text-dim); margin-top: 5px; padding: 0 14px; }

/* Markdown deep styles */
:deep(.chat-bubble h1), :deep(.chat-bubble h2), :deep(.chat-bubble h3) {
  font-size: 15px; font-weight: 600; margin: 10px 0 6px;
  color: var(--text-heading);
}
:deep(.chat-bubble h2) { font-size: 14px; color: var(--blue); }
:deep(.chat-bubble h3) { font-size: 13px; }
:deep(.chat-bubble p) { margin: 6px 0; }
:deep(.chat-bubble ul), :deep(.chat-bubble ol) { margin: 6px 0; padding-left: 22px; }
:deep(.chat-bubble li) { margin: 3px 0; }
:deep(.chat-bubble code) {
  background: rgba(56, 189, 248, 0.12);
  color: #67e8f9;
  border: 1px solid rgba(56, 189, 248, 0.18);
  border-radius: 6px;
  padding: 2px 7px;
  font-size: 13px;
  font-family: "Cascadia Code", "Fira Code", monospace;
}
:deep(.chat-bubble pre) {
  background: #020617;
  color: #e2e8f0;
  padding: 14px 18px;
  border-radius: 12px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.6;
  margin: 10px 0;
  border: 1px solid rgba(148, 163, 184, 0.15);
}
:deep(.chat-bubble pre code) { background: none; color: inherit; padding: 0; border: none; }
:deep(.chat-bubble table) { border-collapse: collapse; width: 100%; margin: 8px 0; font-size: 12px; }
:deep(.chat-bubble th), :deep(.chat-bubble td) {
  border: 1px solid rgba(148, 163, 184, 0.15); padding: 6px 12px; text-align: left;
}
:deep(.chat-bubble th) {
  background: rgba(56, 189, 248, 0.08); font-weight: 600; color: var(--text-primary);
}
:deep(.chat-bubble strong) { font-weight: 600; color: var(--text-heading); }
:deep(.chat-bubble a) { color: var(--blue); }
:deep(.chat-bubble hr) { border: none; border-top: 1px solid rgba(148, 163, 184, 0.12); margin: 12px 0; }
:deep(.chat-bubble blockquote) {
  border-left: 3px solid var(--blue);
  padding-left: 12px;
  margin: 8px 0;
  color: var(--text-muted);
}

/* User message overrides */
.chat-user :deep(.chat-bubble strong) { color: #fff; }
.chat-user :deep(.chat-bubble code) {
  background: rgba(255,255,255,0.15); color: #fff; border-color: rgba(255,255,255,0.25);
}
.chat-user :deep(.chat-bubble a) { color: #bae6fd; }
</style>
