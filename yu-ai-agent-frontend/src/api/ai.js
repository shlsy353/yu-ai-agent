import apiClient from './index'

const BASE_URL = '/ai'

/**
 * AI 恋爱大师 SSE 聊天
 * 使用 fetch + ReadableStream 处理 SSE 流式响应
 */
export function doChatWithLoveAppSse({ message, chatId, onData, onError, onComplete }) {
  const url = `${apiClient.defaults.baseURL}${BASE_URL}/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`

  fetch(url, {
    headers: {
      Accept: 'text/event-stream',
      'Cache-Control': 'no-cache',
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          onComplete && onComplete()
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data === '[DONE]') {
              onComplete && onComplete()
              return
            }
            onData && onData(data)
          }
        }
      }
    })
    .catch((error) => {
      onError && onError(error)
    })
}

/**
 * AI 超级智能体 SSE 聊天
 * 使用 fetch + ReadableStream 处理 SSE 流式响应
 */
export function doChatWithManus({ message, onData, onError, onComplete }) {
  const url = `${apiClient.defaults.baseURL}${BASE_URL}/manus/chat?message=${encodeURIComponent(message)}`

  fetch(url, {
    headers: {
      Accept: 'text/event-stream',
      'Cache-Control': 'no-cache',
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          onComplete && onComplete()
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            if (data === '[DONE]') {
              onComplete && onComplete()
              return
            }
            onData && onData(data)
          }
        }
      }
    })
    .catch((error) => {
      onError && onError(error)
    })
}