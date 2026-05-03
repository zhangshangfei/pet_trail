import request from '../../utils/request'

export const getNotificationList = (params) => request.get('/api/admin/notifications', { params })
export const sendNotification = (data) => request.post('/api/admin/notifications', data)
export const broadcastNotification = (data) => request.post('/api/admin/notifications/broadcast', data)
