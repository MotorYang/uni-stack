import SockJS from 'sockjs-client'
import Stomp, { type Client, type Message as StompMessage, type Frame } from 'stompjs'

// WebSocket 消息格式 (对应后端 WsResponse)
export interface WsMessage<T = unknown> {
  module: string
  type: string
  code: number
  data: T
  timestamp: number
}

// 消息类型常量 (对应后端 WsConstants)
export const WsMessageType = {
  ERROR: 'ERROR',
  SYSTEM_NOTICE: 'SYSTEM_NOTICE',
  CHAT: 'CHAT',
  FORCE_LOGOUT: 'FORCE_LOGOUT',
  REMIND: 'REMIND',
} as const

// 系统公告数据结构
export interface SystemNotice {
  id: string
  title: string
  content: string
  level: 'info' | 'success' | 'warning' | 'error'
  createTime?: string
}

// WebSocket 配置
interface WebSocketConfig {
  url: string
  token: string
  userId: string
  reconnectDelay?: number
  maxReconnectAttempts?: number
}

// 消息回调类型
type MessageCallback<T = unknown> = (message: WsMessage<T>) => void

class WebSocketService {
  private stompClient: Client | null = null
  private config: WebSocketConfig | null = null
  private reconnectAttempts = 0
  private isConnected = false
  private isConnecting = false
  private subscriptions: Map<string, Stomp.Subscription> = new Map()
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private enabled = true // 是否启用 WebSocket

  // 消息处理器
  private systemNoticeHandlers: Set<MessageCallback<SystemNotice>> = new Set()
  private userMessageHandlers: Set<MessageCallback> = new Set()
  private forceLogoutHandlers: Set<() => void> = new Set()

  /**
   * 连接 WebSocket
   */
  connect(config: WebSocketConfig): Promise<void> {
    if (!this.enabled) {
      return Promise.resolve()
    }

    if (this.isConnected || this.isConnecting) {
      return Promise.resolve()
    }

    this.config = {
      reconnectDelay: 30000, // 30秒重试间隔
      maxReconnectAttempts: 3, // 最多重试3次
      ...config,
    }
    this.isConnecting = true

    return new Promise((resolve) => {
      try {
        // 将 token 放到 URL 参数中，供网关鉴权使用
        const wsUrl = `${this.config!.url}?token=${encodeURIComponent(this.config!.token)}`
        const socket = new SockJS(wsUrl)
        this.stompClient = Stomp.over(socket)

        // 禁用控制台调试输出
        this.stompClient.debug = () => {}

        this.stompClient.connect(
          {}, // headers 已通过 URL 参数传递，网关会解析并转发
          (frame?: Frame) => {
            console.log('[WebSocket] Connected:', frame)
            this.isConnected = true
            this.isConnecting = false
            this.reconnectAttempts = 0
            this.subscribeChannels()
            resolve()
          },
          () => {
            console.warn('[WebSocket] Connection failed, server may not be running')
            this.isConnected = false
            this.isConnecting = false
            this.stompClient = null
            this.handleReconnect()
            // 首次连接失败不 reject，避免页面报错
            resolve()
          }
        )
      } catch {
        this.isConnecting = false
        console.warn('[WebSocket] Failed to create connection')
        resolve()
      }
    })
  }

  /**
   * 订阅频道
   */
  private subscribeChannels(): void {
    if (!this.stompClient) return

    // 订阅系统公告频道 (对应后端 TOPIC_PUBLIC)
    const publicSub = this.stompClient.subscribe('/topic/public', (message: StompMessage) => {
      this.handleMessage(message, 'public')
    })
    this.subscriptions.set('public', publicSub)

    // 订阅个人消息队列 (对应后端 QUEUE_USER_REMIND)
    const userSub = this.stompClient.subscribe('/user/queue/remind', (message: StompMessage) => {
      this.handleMessage(message, 'user')
    })
    this.subscriptions.set('user', userSub)
  }

  /**
   * 处理消息
   */
  private handleMessage(message: StompMessage, channel: 'public' | 'user'): void {
    try {
      const wsMessage: WsMessage = JSON.parse(message.body)
      console.log(`[WebSocket] Received ${channel} message:`, wsMessage)

      // 处理强制登出
      if (wsMessage.type === WsMessageType.FORCE_LOGOUT) {
        this.forceLogoutHandlers.forEach((handler) => handler())
        return
      }

      // 处理公共频道消息（系统公告）
      if (channel === 'public') {
        this.systemNoticeHandlers.forEach((handler) =>
          handler(wsMessage as WsMessage<SystemNotice>)
        )
        return
      }

      // 处理个人消息
      if (channel === 'user') {
        this.userMessageHandlers.forEach((handler) => handler(wsMessage))
      }
    } catch (error) {
      console.error('[WebSocket] Failed to parse message:', error)
    }
  }

  /**
   * 重连逻辑
   */
  private handleReconnect(): void {
    if (!this.config || !this.enabled) return

    const { reconnectDelay, maxReconnectAttempts } = this.config

    if (this.reconnectAttempts >= maxReconnectAttempts!) {
      console.warn('[WebSocket] Max reconnect attempts reached, will not retry automatically')
      this.enabled = false // 停止自动重连
      return
    }

    this.reconnectAttempts++
    console.log(
      `[WebSocket] Will reconnect in ${reconnectDelay! / 1000}s (attempt ${this.reconnectAttempts}/${maxReconnectAttempts})`
    )

    // 清除之前的重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    this.reconnectTimer = setTimeout(() => {
      if (this.enabled) {
        this.connect(this.config!)
      }
    }, reconnectDelay)
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.enabled = false

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.stompClient) {
      this.subscriptions.forEach((sub) => sub.unsubscribe())
      this.subscriptions.clear()
      if (this.isConnected) {
        this.stompClient.disconnect(() => {
          console.log('[WebSocket] Disconnected')
        })
      }
      this.stompClient = null
      this.isConnected = false
    }
  }

  /**
   * 手动重新启用并连接
   */
  enable(): void {
    this.enabled = true
    this.reconnectAttempts = 0
    if (this.config) {
      this.connect(this.config)
    }
  }

  /**
   * 注册系统公告处理器
   */
  onSystemNotice(handler: MessageCallback<SystemNotice>): () => void {
    this.systemNoticeHandlers.add(handler)
    return () => this.systemNoticeHandlers.delete(handler)
  }

  /**
   * 注册个人消息处理器
   */
  onUserMessage(handler: MessageCallback): () => void {
    this.userMessageHandlers.add(handler)
    return () => this.userMessageHandlers.delete(handler)
  }

  /**
   * 注册强制登出处理器
   */
  onForceLogout(handler: () => void): () => void {
    this.forceLogoutHandlers.add(handler)
    return () => this.forceLogoutHandlers.delete(handler)
  }

  /**
   * 获取连接状态
   */
  get connected(): boolean {
    return this.isConnected
  }
}

// 导出单例
export const wsService = new WebSocketService()
