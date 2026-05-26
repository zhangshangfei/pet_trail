import request from '@/utils/request'

export const createReport = (data) => {
  return request.post('/api/reports', data)
}

export const getMyReports = () => {
  return request.get('/api/reports/my')
}
