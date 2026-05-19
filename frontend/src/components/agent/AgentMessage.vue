<template>
  <div class="chat-message" :class="`chat-${role}`">
    <div class="chat-bubble-wrap">
      <div v-if="role === 'assistant'" class="chat-avatar chat-avatar-agent">🌡</div>
      <div class="chat-bubble" :class="{ streaming: isStreaming }" v-html="htmlContent" />
      <div v-if="role === 'user'" class="chat-avatar">👤</div>
    </div>
    <div v-if="time" class="chat-time">{{ time }}</div>
    <div v-if="tools && tools.length" class="chat-tools">
      <span v-for="t in tools" :key="t" class="tool-tag">{{ toolLabel(t) }}</span>
    </div>
    <div v-if="dataSource && role === 'assistant'" class="chat-meta">
      数据来源：{{ dataSource }}{{ confidence ? ` | 置信度：${confidence}` : '' }}
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
    // Streaming: use simple inline rendering to avoid full re-parse flicker
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
  // Done: use marked for full markdown
  const raw = marked.parse(props.content, { async: false }) as string
  return raw
})

function escHtml(v: string) {
  return v.replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!))
}
</script>

<style scoped>
.chat-message { margin-bottom: 20px; display: flex; flex-direction: column; }
.chat-message.chat-user { align-items: flex-end; }
.chat-message.chat-agent { align-items: flex-start; }
.chat-bubble-wrap { display: flex; gap: 10px; max-width: 75%; }
.chat-user .chat-bubble-wrap { flex-direction: row-reverse; max-width: 60%; }
.chat-avatar {
  flex-shrink: 0; width: 30px; height: 30px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; font-size: 16px; margin-top: 2px;
}
.chat-avatar-agent {
  background: linear-gradient(135deg, #3b82f6, #7c3aed); color: #fff; font-size: 14px;
}
.chat-bubble {
  padding: 12px 16px; border-radius: 16px; line-height: 1.65; font-size: 14px; word-break: break-word;
}
.chat-user .chat-bubble {
  background: #2563eb; color: #fff; border-bottom-right-radius: 6px;
  box-shadow: 0 2px 8px rgba(37,99,235,0.2);
}
.chat-agent .chat-bubble {
  background: #fff; border: 1px solid rgba(148,163,184,0.22);
  border-bottom-left-radius: 6px; box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.chat-bubble.streaming {
  border-color: #eff6ff; box-shadow: 0 0 0 2px rgba(37,99,235,0.06);
}
.chat-time { font-size: 11px; color: #64748b; margin-top: 5px; padding: 0 14px; }
.chat-tools { display: flex; flex-wrap: wrap; gap: 5px; margin-top: 8px; padding: 0 14px; }
.tool-tag {
  font-size: 10px; color: #2563eb; background: #eff6ff;
  padding: 3px 8px; border-radius: 999px; font-weight: 500;
}
.chat-meta { font-size: 11px; color: #64748b; margin-top: 4px; padding: 0 14px; }

:deep(.chat-bubble h1), :deep(.chat-bubble h2), :deep(.chat-bubble h3) {
  font-size: 15px; font-weight: 600; margin: 10px 0 6px;
}
:deep(.chat-bubble h2) { font-size: 14px; }
:deep(.chat-bubble h3) { font-size: 13px; }
:deep(.chat-bubble p) { margin: 6px 0; }
:deep(.chat-bubble ul), :deep(.chat-bubble ol) { margin: 6px 0; padding-left: 22px; }
:deep(.chat-bubble code) {
  background: rgba(0,0,0,0.05); padding: 2px 5px; border-radius: 4px;
  font-size: 13px; font-family: "Cascadia Code","Fira Code",monospace;
}
:deep(.chat-bubble pre) {
  background: #1e293b; color: #e2e8f0; padding: 12px 16px; border-radius: 10px;
  overflow-x: auto; font-size: 12px; line-height: 1.5; margin: 8px 0;
}
:deep(.chat-bubble pre code) { background: none; color: inherit; padding: 0; }
:deep(.chat-bubble table) { border-collapse: collapse; width: 100%; margin: 8px 0; font-size: 12px; }
:deep(.chat-bubble th), :deep(.chat-bubble td) { border: 1px solid #e5edf7; padding: 5px 10px; text-align: left; }
:deep(.chat-bubble th) { background: #f8fbff; font-weight: 600; }
:deep(.chat-bubble strong) { font-weight: 600; }
:deep(.chat-bubble hr) { border: none; border-top: 1px solid #e5edf7; margin: 10px 0; }
</style>
