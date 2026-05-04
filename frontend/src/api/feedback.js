import request from '@/utils/request'

export const createFeedback = (data) => {
  return request.post('/api/feedback', data)
}

export const getMyFeedback = (params) => {
  return request.get('/api/feedback/my', params)
}
