<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="$router.push('/')">← 返回</button>
      <h2>🤖 AI 超级智能体</h2>
    </div>

    <div class="chat-messages" ref="messagesRef">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']"
      >
        <div class="message-content">
          {{ msg.content }}
        </div>
      </div>
      <div v-if="loading" class="message message-ai">
        <div class="message-content">
          <span class="typing">{{ statusText || '...' }}</span>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <input
        v-model="inputText"
        type="text"
        placeholder="请输入你的问题..."
        :disabled="loading"
        @keyup.enter="sendMessage"
      />
      <button :disabled="loading || !inputText.trim()" @click="sendMessage">
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { doChatWithManus } from '../api/ai.js'

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const statusText = ref('')
const messagesRef = ref(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  let aiContent = ''

  doChatWithManus({
    message: text,
    onData(data) {
      // 区分状态提示和最终结果
      if (data.startsWith('[STATUS]')) {
        statusText.value = data.slice(8)
        return
      }
      if (data.startsWith('[RESULT]')) {
        aiContent = data.slice(8)
        statusText.value = ''
      } else {
        // 兼容旧格式（无前缀）
        aiContent += data
      }

      if (aiContent) {
        if (messages.value.length > 0 && messages.value[messages.value.length - 1].role === 'ai') {
          messages.value[messages.value.length - 1].content = aiContent
        } else {
          messages.value.push({ role: 'ai', content: aiContent })
        }
      }
      scrollToBottom()
    },
    onError(error) {
      console.error('SSE error:', error)
      messages.value.push({ role: 'ai', content: '抱歉，连接出错了，请稍后重试。' })
      loading.value = false
      statusText.value = ''
      scrollToBottom()
    },
    onComplete() {
      loading.value = false
      statusText.value = ''
      scrollToBottom()
    },
  })
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background: #f5f5f5;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #eee;
  gap: 12px;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  flex: 1;
}

.back-btn {
  background: none;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #f0f0f0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  max-width: 70%;
}

.message-user {
  align-self: flex-end;
}

.message-ai {
  align-self: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-user .message-content {
  background: #1a73e8;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-ai .message-content {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.typing {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.chat-input {
  display: flex;
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #eee;
  gap: 12px;
}

.chat-input input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-input input:focus {
  border-color: #1a73e8;
}

.chat-input button {
  padding: 12px 24px;
  background: #1a73e8;
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.chat-input button:hover:not(:disabled) {
  background: #1557b0;
}

.chat-input button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>