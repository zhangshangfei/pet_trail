import request from '@/utils/request'

export const getNotifications = (params = {}) => {
  return request.get('/api/notifications', params)
}

export const getUnreadCount = () => {
  return request.get('/api/notifications/unread-count')
}

export const markAsRead = (id) => {
  return request.put('/api/notifications/' + id + '/read')
}

export const markAllAsRead = () => {
  return request.put('/api/notifications/read-all')
}

export const deleteNotification = (id) => {
  return request.delete('/api/notifications/' + id)
}

export const clearAllNotifications = () => {
  return request.delete('/api/notifications/clear')
}

export const getUnreadSystem = () => {
  return request.get('/api/notifications/unread-system')
}

export const pollNotifications = () => {
  return request.get('/api/notifications/poll')
}
