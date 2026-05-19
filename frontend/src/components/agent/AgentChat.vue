<template>
  <BaseCard class="chat-panel">
    <div class="chat-header">
      <div class="chat-header-left">
        <span class="chat-header-icon">●</span>
        <h3>智能会话</h3>
      </div>
      <button class="btn-text" @click="clear">清空会话</button>
    </div>

    <div ref="chatBodyRef" class="chat-body scrollbar-thin">
      <div v-if="!session || session.messages.length === 0" class="chat-welcome">
        <div class="welcome-icon">🌡</div>
        <p>我是 <strong>温度计 Agent</strong>，你的环境监测智能助手。</p>
        <p>可以直接提问，或使用右侧工具与快捷提问获取精准分析。</p>
        <p class="chat-note">数据源：<code>{{ source }}</code></p>
      </div>

      <AgentMessage
        v-for="msg in session?.messages"
        :key="msg.id || msg.createdAt"
        :role="msg.role"
        :content="msg.content"
        :time="msg.createdAt"
        :tools="msg.usedTools"
        :data-source="msg.dataSource"
        :confidence="msg.confidence"
        :is-streaming="msg.role === 'assistant' && isStreaming && msg === lastAssistantMsg"
      />

      <div v-if="isStreaming && !lastAssistantMsg" class="chat-welcome">
        <p>正在连接...</p>
      </div>
    </div>

    <div class="chat-footer">
      <AgentInput
        :disabled="isStreaming"
        @send="send"
        @clear="clear"
      />
      <p class="chat-hint">Enter 发送 · Shift+Enter 换行</p>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import BaseCard from '@/components/common/BaseCard.vue'
import AgentMessage from './AgentMessage.vue'
import AgentInput from './AgentInput.vue'
import { useAgentStore } from '@/stores/agent'
import { agentApi } from '@/api/agent'
import type { ChatMessage } from '@/types'

const props = defineProps<{
  source: string
}>()

const agentStore = useAgentStore()
const chatBodyRef = ref<HTMLDivElement>()

const session = computed(() => agentStore.currentSession)
const isStreaming = computed(() => agentStore.isStreaming)

const lastAssistantMsg = computed(() => {
  const msgs = session.value?.messages || []
  for (let i = msgs.length - 1; i >= 0; i--) {
    if (msgs[i].role === 'assistant') return msgs[i]
  }
  return null
})

function scrollBottom() {
  nextTick(() => {
    const el = chatBodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function clear() {
  agentStore.clearCurrentSession()
}

function getLastAssistantMessage(sessionId: string) {
  const s = agentStore.sessions.find(x => x.id === sessionId)
  if (!s) return null
  for (let i = s.messages.length - 1; i >= 0; i--) {
    if (s.messages[i].role === 'assistant') return s.messages[i]
  }
  return null
}

async function send(message: string) {
  if (agentStore.isStreaming) return

  const sess = agentStore.ensureSession(message)
  agentStore.isStreaming = true

  const userMsg: ChatMessage = {
    role: 'user', content: message, createdAt: formatTime(new Date()),
  }
  agentStore.addMessage(sess.id, userMsg)
  scrollBottom()

  const agentTime = formatTime(new Date())
  const agentMsg: ChatMessage = {
    role: 'assistant', content: '', createdAt: agentTime,
  }
  agentStore.addMessage(sess.id, agentMsg)
  scrollBottom()

  let fullContent = ''

  try {
    const history = agentStore.getHistory(sess)
    const resp = await agentApi.stream({
      sessionId: sess.id,
      role: '环境监测助手',
      message,
      source: props.source,
      history,
    })

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    const reader = resp.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let currentEvent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.substring(6).trim()
        } else if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (currentEvent === 'chunk') {
            fullContent += data
            const live = getLastAssistantMessage(sess.id)
            if (live) live.content = fullContent
            scrollBottom()
          } else if (currentEvent === 'done') {
            try {
              const doneData = JSON.parse(data)
              const live = getLastAssistantMessage(sess.id)
              if (live) {
                live.usedTools = doneData.usedTools || []
                live.dataSource = doneData.dataSource || ''
                live.confidence = doneData.confidence || ''
              }
            } catch { /* ignore */ }
          } else if (currentEvent === 'error') {
            const live = getLastAssistantMessage(sess.id)
            if (live) live.content = fullContent || '请求失败：' + data
          }
          currentEvent = ''
        }
      }
    }
  } catch (err: any) {
    const live = getLastAssistantMessage(sess.id)
    if (live) live.content = fullContent || `请求失败：${err.message}`
  }

  agentStore.isStreaming = false
  scrollBottom()
}

function onQuickPrompt(e: Event) {
  const q = (e as CustomEvent).detail as string
  if (q) send(q)
}

onMounted(() => {
  window.addEventListener('agent-quick-prompt', onQuickPrompt)
})
onUnmounted(() => {
  window.removeEventListener('agent-quick-prompt', onQuickPrompt)
})

watch(() => session.value?.messages.length, scrollBottom)

function formatTime(d: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 520px;
  max-height: calc(100vh - 280px);
  overflow: hidden;
}
.chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  flex-shrink: 0;
}
.chat-header-left { display: flex; align-items: center; gap: 8px; }
.chat-header-icon {
  color: var(--green);
  font-size: 10px;
  animation: breath 2s ease-in-out infinite;
}
@keyframes breath {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
.chat-header h3 { font-size: 15px; font-weight: 600; color: var(--text-heading); }
.btn-text {
  background: none; border: 1px solid rgba(148,163,184,0.15); color: var(--text-muted); cursor: pointer;
  font-size: 11px; padding: 4px 10px; border-radius: 6px; transition: all 0.2s;
}
.btn-text:hover { color: var(--red); border-color: rgba(239,68,68,0.3); background: rgba(239,68,68,0.08); }

.chat-body {
  flex: 1; padding: 20px 22px; overflow-y: auto;
  background: rgba(2, 6, 23, 0.35);
  scroll-behavior: smooth; min-height: 0;
}
.chat-welcome { text-align: center; padding: 60px 20px; }
.chat-welcome p { color: var(--text-muted); margin-bottom: 10px; line-height: 1.7; font-size: 14px; }
.chat-welcome strong { color: var(--text-primary); }
.chat-note { font-size: 12px !important; }
.chat-note code {
  background: rgba(56,189,248,0.12); color: var(--blue); padding: 2px 7px;
  border-radius: 4px; font-family: "Cascadia Code", "Fira Code", monospace; font-size: 11px;
}
.welcome-icon { font-size: 40px; margin-bottom: 16px; }

.chat-footer {
  padding: 16px 22px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
  flex-shrink: 0;
}
.chat-hint { font-size: 11px; color: var(--text-dim); text-align: center; margin-top: 8px; }
</style>
