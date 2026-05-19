import http from './http'
import type { AgentStatus, AgentSession } from '@/types'

export const agentApi = {
  status() {
    return http.get<AgentStatus>('/api/agent/status')
  },
  stream(body: {
    sessionId: string
    role: string
    message: string
    source: string
    history: { role: string; content: string }[]
  }) {
    return fetch('/api/agent/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },
  sessions() {
    return http.get<AgentSession[]>('/api/agent/sessions')
  },
}
