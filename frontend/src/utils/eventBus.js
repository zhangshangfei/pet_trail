// 简单的事件总线
class EventBus {
  constructor() {
    this.events = {}
  }
  
  on(event, callback) {
    if (!this.events[event]) {
      this.events[event] = []
    }
    this.events[event].push(callback)
  }
  
  emit(event, data) {
    if (this.events[event]) {
      this.events[event].forEach(callback => {
        try {
          callback(data)
        } catch (e) {
          console.error('EventBus emit error:', e)
        }
      })
    }
  }
}

export default new EventBus()
