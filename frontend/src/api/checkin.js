import request from '@/utils/request'

export const getCheckinItems = () => {
  return request.get('/api/checkin/items')
}

export const checkin = (data) => {
  return request.post('/api/checkin', data)
}

export const getCalendar = (year, month, noCache = false) => {
  return request.get('/api/checkin/calendar', { year, month }, {}, noCache ? { cache: false } : {})
}

export const cancelCheckin = (id) => {
  return request.post(`/api/checkin/${id}/cancel`)
}

export const getStats = () => {
  return request.get('/api/checkin/stats')
}

export const createCheckinItem = (data) => {
  return request.post('/api/checkin/items', data)
}

export const updateCheckinItem = (id, data) => {
  return request.put(`/api/checkin/items/${id}`, data)
}

export const deleteCheckinItem = (id) => {
  return request.delete(`/api/checkin/items/${id}`)
}

export const hideCheckinItem = (id) => {
  return request.post(`/api/checkin/items/${id}/hide`)
}

export const showCheckinItem = (id) => {
  return request.post(`/api/checkin/items/${id}/show`)
}

export const getCheckinReport = (period = 'week') => {
  return request.get('/api/checkin/report', { period })
}

export const getCheckinReminders = () => {
  return request.get('/api/checkin/reminders')
}

export const createCheckinReminder = (data) => {
  return request.post('/api/checkin/reminders', data)
}

export const updateCheckinReminder = (id, data) => {
  return request.put(`/api/checkin/reminders/${id}`, data)
}

export const deleteCheckinReminder = (id) => {
  return request.delete(`/api/checkin/reminders/${id}`)
}
