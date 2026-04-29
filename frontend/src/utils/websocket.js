import env from '@/config/env'

class WebSocketManager {
  constructor() {
    this.ws = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
    this.heartbeatInterval = 30000
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.listeners = new Map()
    this.token = ''
  }

  connect(token) {
    if (this.ws && this.isConnected) return
    this.token = token || uni.getStorageSync('token')
    if (!this.token) return

    let wsUrl = this.getWebSocketUrl()
    if (!wsUrl) return

    try {
      this.ws = uni.connectSocket({
        url: wsUrl + '?token=' + this.token,
        complete: () => {}
      })

      this.ws.onOpen(() => {
        this.isConnected = true
        this.reconnectAttempts = 0
        this.startHeartbeat()
        this.emit('open')
      })

      this.ws.onMessage((res) => {
        try {
          const data = JSON.parse(res.data)
          this.emit('message', data)

          if (data.type === 'notification') {
            this.emit('notification', data)
          } else if (data.type === 'unreadCount') {
            this.emit('unreadCount', data)
          } else if (data.type === 'init') {
            this.emit('init', data)
          }
        } catch (e) {
          // ignore parse errors
        }
      })

      this.ws.onClose(() => {
        this.isConnected = false
        this.stopHeartbeat()
        this.emit('close')
        this.tryReconnect()
      })

      this.ws.onError(() => {
        this.isConnected = false
        this.stopHeartbeat()
        this.emit('error')
        this.tryReconnect()
      })
    } catch (e) {
      console.error('WebSocket连接失败:', e)
      this.tryReconnect()
    }
  }

  getWebSocketUrl() {
    let apiBase = env.VITE_API_BASE_URL
    if (apiBase && apiBase !== 'cloud') {
      const wsProtocol = apiBase.startsWith('https') ? 'wss' : 'ws'
      return apiBase.replace(/^https?/, wsProtocol).replace(/\/$/, '') + '/ws/notifications'
    }
    return 'wss://springboot-4fyd-243081-4-1419682950.sh.run.tcloudbase.com/ws/notifications'
  }

  disconnect() {
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      try {
        this.ws.close({})
      } catch (e) {
        // ignore
      }
      this.ws = null
    }
    this.isConnected = false
    this.reconnectAttempts = 0
  }

  tryReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return
    this.reconnectAttempts++
    let delay = this.reconnectInterval * this.reconnectAttempts
    this.reconnectTimer = setTimeout(() => {
      this.connect(this.token)
    }, delay)
  }

  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected && this.ws) {
        try {
          this.ws.send({
            data: JSON.stringify({ action: 'ping' })
          })
        } catch (e) {
          // ignore
        }
      }
    }, this.heartbeatInterval)
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  off(event, callback) {
    if (!this.listeners.has(event)) return
    if (callback) {
      const list = this.listeners.get(event)
      const idx = list.indexOf(callback)
      if (idx >= 0) list.splice(idx, 1)
    } else {
      this.listeners.delete(event)
    }
  }

  emit(event, data) {
    if (!this.listeners.has(event)) return
    this.listeners.get(event).forEach(cb => {
      try {
        cb(data)
      } catch (e) {
        // ignore
      }
    })
  }
}

const wsManager = new WebSocketManager()

export default wsManager
