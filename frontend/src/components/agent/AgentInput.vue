<template>
  <div class="chat-input-wrapper" :class="{ focused }">
    <textarea
      v-model="text"
      rows="2"
      placeholder="输入问题，例如：分析当前温度变化趋势..."
      @keydown="onKeydown"
      @focus="focused = true"
      @blur="focused = false"
    />
    <div class="chat-input-row">
      <div class="chat-input-actions">
        <button class="btn-ghost" title="附加上下文" @click="emit('attach')">📎 上下文</button>
        <button class="btn-ghost" title="使用工具" @click="emit('tools')">🔧 工具</button>
      </div>
      <div class="chat-input-btns">
        <button class="btn-ghost" @click="emit('clear')">🗑</button>
        <button class="btn-send" :disabled="disabled || !text.trim()" @click="send">
          <span class="send-icon">➤</span> 发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  disabled?: boolean
}>()

const emit = defineEmits<{
  send: [string]
  clear: []
  attach: []
  tools: []
}>()

const text = ref('')
const focused = ref(false)

function send() {
  const msg = text.value.trim()
  if (!msg) return
  text.value = ''
  emit('send', msg)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}
</script>

<style scoped>
.chat-input-wrapper {
  border: 1.5px solid rgba(56, 189, 248, 0.25);
  border-radius: 16px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: rgba(2, 6, 23, 0.65);
  box-shadow: 0 0 0 1px rgba(56, 189, 248, 0.06);
}
.chat-input-wrapper.focused {
  border-color: var(--blue);
  box-shadow: 0 0 24px rgba(56, 189, 248, 0.15);
}
.chat-input-wrapper textarea {
  width: 100%; border: none; padding: 12px 16px; font-size: 14px; resize: none;
  min-height: 52px; max-height: 100px; font-family: inherit; line-height: 1.6; outline: none;
  background: transparent; color: var(--text-primary);
}
.chat-input-wrapper textarea::placeholder { color: var(--text-dim); }
.chat-input-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px;
  background: rgba(15, 23, 42, 0.45);
  border-top: 1px solid rgba(148, 163, 184, 0.10);
}
.chat-input-actions { display: flex; gap: 2px; }
.chat-input-btns { display: flex; gap: 8px; align-items: center; }
.btn-ghost {
  background: none; border: 1px solid transparent; border-radius: 8px;
  padding: 5px 10px; font-size: 12px; cursor: pointer; color: var(--text-muted);
  transition: all 0.15s; white-space: nowrap;
}
.btn-ghost:hover { background: rgba(148, 163, 184, 0.08); color: var(--text-primary); }
.btn-send {
  min-height: 34px; border: none;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; border-radius: 10px; padding: 6px 20px; cursor: pointer; font-weight: 600;
  font-size: 13px; transition: all 0.2s;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.30);
  display: flex; align-items: center; gap: 4px;
}
.btn-send:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(37, 99, 235, 0.40), 0 0 20px rgba(139, 92, 246, 0.15);
}
.btn-send:disabled { opacity: 0.4; cursor: not-allowed; transform: none; box-shadow: none; }
.send-icon { font-size: 14px; }
</style>
