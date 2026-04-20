import request from '@/utils/request'

export const recordBehavior = (data) => {
  return request.post('/api/behaviors', data)
}

export const recordView = (data) => {
  return request.post('/api/behaviors/view', data)
}
