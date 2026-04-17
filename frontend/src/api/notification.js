import request from '@/utils/request'

export const getNotifications = (page = 1, size = 20) => {
  return request.get('/api/notifications', { page, size })
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
