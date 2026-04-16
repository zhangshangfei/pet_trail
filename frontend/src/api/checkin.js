import request from '@/utils/request'

/**
 * 获取打卡项列表
 */
export const getCheckinItems = () => {
  return request.get('/api/checkin/items')
}

/**
 * 打卡
 */
export const checkin = (data) => {
  return request.post('/api/checkin', data)
}

/**
 * 获取打卡日历
 */
export const getCalendar = (year, month) => {
  return request.get('/api/checkin/calendar', { year, month })
}

/**
 * 取消打卡
 */
export const cancelCheckin = (id) => {
  return request.post(`/api/checkin/${id}/cancel`)
}

/**
 * 获取打卡统计
 */
export const getStats = () => {
  return request.get('/api/checkin/stats')
}
