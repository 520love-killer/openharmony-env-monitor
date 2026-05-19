<template>
  <div class="chat-input-wrapper" :class="{ focused }">
    <textarea
      v-model="text"
      rows="2"
      placeholder="请输入你的问题，例如：分析当前温度变化趋势..."
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
        <button class="btn-ghost" @click="emit('clear')">🗑 清空</button>
        <button class="btn-primary" :disabled="disabled || !text.trim()" @click="send">➤ 发送</button>
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
  border: 1.5px solid rgba(148,163,184,0.22);
  border-radius: 16px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #fff;
}
.chat-input-wrapper.focused {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37,99,235,0.08);
}
.chat-input-wrapper textarea {
  width: 100%; border: none; padding: 12px 14px; font-size: 14px; resize: none;
  min-height: 52px; max-height: 100px; font-family: inherit; line-height: 1.5; outline: none;
  background: transparent; color: #1f2937;
}
.chat-input-wrapper textarea::placeholder { color: #9ca3af; }
.chat-input-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; background: #f8fbff; border-top: 1px solid #eef3f9;
}
.chat-input-actions { display: flex; gap: 2px; }
.chat-input-btns { display: flex; gap: 8px; }
.btn-ghost {
  background: none; border: 1px solid transparent; border-radius: 8px;
  padding: 5px 10px; font-size: 12px; cursor: pointer; color: #64748b;
  transition: all 0.15s; white-space: nowrap;
}
.btn-ghost:hover { background: #f3f4f6; color: #1f2937; }
.btn-primary {
  min-height: 32px; border: none; background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff; border-radius: 8px; padding: 6px 18px; cursor: pointer; font-weight: 600;
  font-size: 13px; transition: all 0.15s; box-shadow: 0 2px 6px rgba(37,99,235,0.25);
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 10px rgba(37,99,235,0.3); }
.btn-primary:disabled { opacity: 0.45; cursor: not-allowed; transform: none; box-shadow: none; }
</style>
