import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { wsService, type WsMessage, type SystemNotice } from '@/services/websocket'
import { useUserStore } from './user'
import { getUserIdFromToken } from '@/utils/jwt'

// 消息接口
export interface Message {
  id: string
  title: string
  content: string
  type: 'info' | 'success' | 'warning' | 'error'
  time: string
  timestamp: number
  read: boolean
  module?: string
}

// 时间格式化
function formatTime(timestamp: number): string {
  const now = Date.now()
  const diff = now - timestamp

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)} 分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)} 小时前`
  } else {
    const date = new Date(timestamp)
    return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
}

export const useMessageStore = defineStore('message', () => {
  // 消息列表
  const messages = ref<Message[]>([])

  // WebSocket 连接状态
  const wsConnected = ref(false)

  // 计算属性
  const unreadMessages = computed(() => messages.value.filter((m) => !m.read))
  const readMessages = computed(() => messages.value.filter((m) => m.read).slice(0, 5))
  const historyMessages = computed(() => messages.value)
  const unreadCount = computed(() => unreadMessages.value.length)

  /**
   * 添加消息
   */
  function addMessage(msg: Omit<Message, 'id' | 'time' | 'read'>) {
    const newMsg: Message = {
      ...msg,
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      time: formatTime(msg.timestamp),
      read: false,
    }
    messages.value.unshift(newMsg)

    // 最多保留 100 条消息
    if (messages.value.length > 100) {
      messages.value = messages.value.slice(0, 100)
    }

    return newMsg
  }

  /**
   * 标记消息已读
   */
  function markAsRead(id: string) {
    const msg = messages.value.find((m) => m.id === id)
    if (msg) {
      msg.read = true
    }
  }

  /**
   * 全部标记已读
   */
  function markAllAsRead() {
    messages.value.forEach((m) => (m.read = true))
  }

  /**
   * 删除消息
   */
  function deleteMessage(id: string) {
    const index = messages.value.findIndex((m) => m.id === id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
  }

  /**
   * 处理系统公告
   */
  function handleSystemNotice(wsMsg: WsMessage<SystemNotice | string>) {
    const rawData = wsMsg.data

    // 如果是字符串，尝试解析为 JSON
    if (typeof rawData === 'string') {
      let parsed: Record<string, unknown> | null = null
      try {
        parsed = JSON.parse(rawData) as Record<string, unknown>
      } catch {
        // 解析失败，作为纯文本处理
        addMessage({
          title: '系统通知',
          content: rawData,
          type: 'info',
          timestamp: wsMsg.timestamp,
          module: wsMsg.module,
        })
        return
      }
      // 解析成功
      addMessage({
        title: (parsed.title as string) || '系统通知',
        content: (parsed.content as string) || (parsed.message as string) || '',
        type: (parsed.level as Message['type']) || 'info',
        timestamp: wsMsg.timestamp,
        module: wsMsg.module,
      })
      return
    }

    // 标准 SystemNotice 对象
    const notice = rawData as SystemNotice
    addMessage({
      title: notice.title || '系统通知',
      content: notice.content || '',
      type: notice.level || 'info',
      timestamp: wsMsg.timestamp,
      module: wsMsg.module,
    })
  }

  /**
   * 处理个人消息
   */
  function handleUserMessage(wsMsg: WsMessage) {
    const data = wsMsg.data as { title?: string; content?: string; level?: string }
    addMessage({
      title: data.title || '新消息',
      content: data.content || '',
      type: (data.level as Message['type']) || 'info',
      timestamp: wsMsg.timestamp,
      module: wsMsg.module,
    })
  }

  /**
   * 初始化 WebSocket 连接
   */
  async function initWebSocket() {
    const userStore = useUserStore()

    if (!userStore.token) {
      console.warn('[MessageStore] No token, skip WebSocket connection')
      return
    }

    const userId = getUserIdFromToken(userStore.token)
    if (!userId) {
      console.warn('[MessageStore] Invalid token, skip WebSocket connection')
      return
    }

    // 获取 WebSocket URL
    const wsUrl = import.meta.env.VITE_WS_URL || '/ws'

    // 注册消息处理器（只注册一次）
    wsService.onSystemNotice(handleSystemNotice)
    wsService.onUserMessage(handleUserMessage)
    wsService.onForceLogout(() => {
      console.log('[MessageStore] Force logout received')
      userStore.logout()
      window.location.href = '/login'
    })

    // 连接 WebSocket
    await wsService.connect({
      url: wsUrl,
      token: userStore.token,
      userId,
    })

    // 检查连接状态
    wsConnected.value = wsService.connected
  }

  /**
   * 断开 WebSocket
   */
  function disconnectWebSocket() {
    wsService.disconnect()
    wsConnected.value = false
  }

  /**
   * 更新连接状态
   */
  function updateConnectionStatus() {
    wsConnected.value = wsService.connected
  }

  /**
   * 手动重连
   */
  function reconnect() {
    wsService.enable()
    wsConnected.value = wsService.connected
  }

  /**
   * 刷新消息时间显示
   */
  function refreshMessageTimes() {
    messages.value.forEach((m) => {
      m.time = formatTime(m.timestamp)
    })
  }

  return {
    messages,
    wsConnected,
    unreadMessages,
    readMessages,
    historyMessages,
    unreadCount,
    addMessage,
    markAsRead,
    markAllAsRead,
    deleteMessage,
    initWebSocket,
    disconnectWebSocket,
    refreshMessageTimes,
    updateConnectionStatus,
    reconnect,
  }
})
