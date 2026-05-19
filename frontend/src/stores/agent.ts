import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { agentApi } from '@/api/agent'
import type { AgentStatus, AgentSession, ChatMessage } from '@/types'

const SESSIONS_KEY = 'thermometer-agent-sessions'
const MAX_HISTORY_MSGS = 10
const MAX_MSG_LENGTH = 1000
const MAX_SESSIONS = 20

function loadSessions(): AgentSession[] {
  try {
    const raw = localStorage.getItem(SESSIONS_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

function saveSessions(sessions: AgentSession[]) {
  try {
    if (sessions.length > MAX_SESSIONS) sessions = sessions.slice(0, MAX_SESSIONS)
    localStorage.setItem(SESSIONS_KEY, JSON.stringify(sessions))
  } catch { /* ignore */ }
}

export const useAgentStore = defineStore('agent', () => {
  const source = ref('REAL_SERIAL')
  const currentPage = ref('agent')
  const agentStatus = ref<AgentStatus | null>(null)
  const sessions = ref<AgentSession[]>(loadSessions())
  const currentSessionId = ref<string | null>(null)
  const isStreaming = ref(false)

  const currentSession = computed(() =>
    sessions.value.find(s => s.id === currentSessionId.value) || null
  )

  function setSource(s: string) {
    source.value = s
    refreshCurrentPage()
  }

  function setPage(page: string) {
    currentPage.value = page
  }

  async function refreshCurrentPage() {
    // Views handle their own refresh via watch on source
  }

  async function checkStatus() {
    try {
      const res = await agentApi.status()
      agentStatus.value = res.data
    } catch { /* ignore */ }
  }

  function createSession(firstMessage?: string): AgentSession {
    const id = 'session-' + Date.now()
    const title = firstMessage
      ? (firstMessage.length > 50 ? firstMessage.substring(0, 50) + '...' : firstMessage)
      : '新会话'
    const session: AgentSession = {
      id, title, role: '环境监测助手',
      messages: [],
      createdAt: formatTime(new Date()),
      updatedAt: formatTime(new Date()),
    }
    sessions.value.unshift(session)
    saveSessions(sessions.value)
    currentSessionId.value = id
    return session
  }

  function ensureSession(firstMessage?: string): AgentSession {
    if (!currentSession.value) {
      const existing = sessions.value[0]
      if (existing) {
        currentSessionId.value = existing.id
        return existing
      }
      return createSession(firstMessage)
    }
    return currentSession.value
  }

  function addMessage(sessionId: string, msg: ChatMessage) {
    const s = sessions.value.find(x => x.id === sessionId)
    if (!s) return
    s.messages.push(msg)
    s.updatedAt = formatTime(new Date())
    if (s.messages.length <= 2) {
      const first = s.messages[0]?.content || ''
      s.title = first.length > 50 ? first.substring(0, 50) + '...' : first
    }
    saveSessions(sessions.value)
  }

  function clearCurrentSession() {
    if (currentSessionId.value) {
      sessions.value = sessions.value.filter(s => s.id !== currentSessionId.value)
      saveSessions(sessions.value)
      currentSessionId.value = null
    }
  }

  function getHistory(session: AgentSession) {
    const recent = session.messages.slice(-MAX_HISTORY_MSGS)
    return recent.map(m => ({
      role: m.role,
      content: m.content && m.content.length > MAX_MSG_LENGTH
        ? m.content.substring(0, MAX_MSG_LENGTH) + '...'
        : m.content,
    }))
  }

  return {
    source, currentPage, agentStatus, sessions, currentSessionId,
    currentSession, isStreaming,
    setSource, setPage, checkStatus, refreshCurrentPage,
    createSession, ensureSession, addMessage, clearCurrentSession, getHistory,
  }
})

function formatTime(d: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
