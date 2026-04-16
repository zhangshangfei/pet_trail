import request from '@/utils/request'

/**
 * 获取体重记录列表
 * @param {number} petId - 宠物 ID
 */
export const getWeightRecords = (petId) => {
  return request.get(`/api/pets/${petId}/weight-records`)
}

/**
 * 获取时间范围内的体重记录
 * @param {number} petId - 宠物 ID
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 */
export const getWeightRecordsByRange = (petId, startDate, endDate) => {
  return request.get(`/api/pets/${petId}/weight-records/range`, { startDate, endDate })
}

/**
 * 获取最后一条体重记录
 * @param {number} petId - 宠物 ID
 */
export const getLastWeightRecord = (petId) => {
  return request.get(`/api/pets/${petId}/weight-records/last`)
}

/**
 * 获取体重趋势
 * @param {number} petId - 宠物 ID
 * @param {number} days - 天数，默认 7
 */
export const getWeightTrend = (petId, days = 7) => {
  return request.get(`/api/pets/${petId}/weight-records/trend`, { days })
}

/**
 * 创建体重记录
 * @param {number} petId - 宠物 ID
 * @param {number} weight - 体重
 * @param {string} recordDate - 记录日期
 */
export const createWeightRecord = (petId, weight, recordDate) => {
  return request.post(`/api/pets/${petId}/weight-records`, { weight, recordDate })
}

/**
 * 删除体重记录
 * @param {number} petId - 宠物 ID
 * @param {number} recordId - 记录 ID
 */
export const deleteWeightRecord = (petId, recordId) => {
  return request.delete(`/api/pets/${petId}/weight-records/${recordId}`)
}

/**
 * 记录步数
 * @param {number} steps - 步数
 * @param {number} distance - 距离 (km)
 * @param {string} recordDate - 记录日期
 * @param {number} petId - 宠物 ID
 */
export const recordStep = (steps, distance, recordDate, petId) => {
  return request.post('/api/health/steps', { steps, distance, record_date: recordDate, pet_id: petId })
}

/**
 * 记录饮水量
 * @param {number} amount - 水量 (ml)
 * @param {string} recordDate - 记录日期
 * @param {string} recordTime - 记录时间
 * @param {number} petId - 宠物 ID
 */
export const recordWater = (amount, recordDate, recordTime, petId) => {
  return request.post('/api/health/water', { amount, record_date: recordDate, record_time: recordTime, pet_id: petId })
}

/**
 * 获取健康数据看板
 * @param {number} petId - 宠物 ID
 */
export const getHealthDashboard = (petId) => {
  return request.get('/api/health/dashboard', { pet_id: petId })
}
