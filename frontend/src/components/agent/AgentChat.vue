<template>
  <BaseCard class="chat-panel">
    <div class="chat-header">
      <h3>💬 智能会话</h3>
      <button class="btn-text" @click="clear">清空会话</button>
    </div>

    <div ref="chatBodyRef" class="chat-body scrollbar-thin">
      <div v-if="!session || session.messages.length === 0" class="chat-welcome">
        <p>👋 你好！我是 <strong>温度计</strong>，你的环境监测智能助手。</p>
        <p>你可以直接提问，或使用右侧工具与快捷提问获取更精准的分析。</p>
        <p class="chat-note">当前优先使用 <strong>{{ source }}</strong> 数据。如果只有 MOCK 数据，我会明确说明。</p>
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
      <p class="chat-hint">你可以直接提问，或使用右侧工具与快捷提问获取更精准的分析。</p>
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
const quickPrompt = ref('')

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
  padding: 14px 22px; border-bottom: 1px solid #eef3f9; flex-shrink: 0;
}
.chat-header h3 { font-size: 15px; font-weight: 600; color: #0f172a; }
.btn-text {
  background: none; border: none; color: #64748b; cursor: pointer;
  font-size: 12px; padding: 4px 8px; border-radius: 6px; transition: all 0.15s;
}
.btn-text:hover { color: #ef4444; background: #fef2f2; }
.chat-body {
  flex: 1; padding: 20px 22px; overflow-y: auto; background: #f5f8fc;
  scroll-behavior: smooth; min-height: 0;
}
.chat-welcome { text-align: center; padding: 60px 20px; }
.chat-welcome p { color: #64748b; margin-bottom: 8px; line-height: 1.6; font-size: 14px; }
.chat-note { font-size: 12px !important; }
.chat-footer { padding: 16px 22px; border-top: 1px solid #eef3f9; background: #fff; flex-shrink: 0; }
.chat-hint { font-size: 11px; color: #64748b; text-align: center; margin-top: 8px; }
</style>
