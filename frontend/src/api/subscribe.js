import request from '@/utils/request'

export const getSubscribeTemplates = () => {
  return request.get('/api/wx-subscribe/templates')
}

export const authorizeSubscribe = (templateType, count = 1) => {
  return request.post('/api/wx-subscribe/authorize', { templateType, count })
}
