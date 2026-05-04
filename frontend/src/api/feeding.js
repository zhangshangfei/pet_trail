import request from '@/utils/request'

export const getFeedingReminders = () => {
  return request.get('/api/feeding-reminders')
}

export const createFeedingReminder = (data) => {
  return request.post('/api/feeding-reminders', data)
}

export const updateFeedingReminder = (id, data) => {
  return request.put(`/api/feeding-reminders/${id}`, data)
}

export const toggleFeedingReminder = (id) => {
  return request.put(`/api/feeding-reminders/${id}/toggle`)
}

export const deleteFeedingReminder = (id) => {
  return request.delete(`/api/feeding-reminders/${id}`)
}
