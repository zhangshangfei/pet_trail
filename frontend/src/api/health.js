import request from '@/utils/request'

export const getWeightRecords = (petId) => {
  return request.get(`/api/pets/${petId}/weight-records`)
}

export const getWeightRecordsByRange = (petId, startDate, endDate) => {
  return request.get(`/api/pets/${petId}/weight-records/range`, { startDate, endDate })
}

export const getLastWeightRecord = (petId) => {
  return request.get(`/api/pets/${petId}/weight-records/last`)
}

export const getWeightTrend = (petId, days = 7) => {
  return request.get(`/api/pets/${petId}/weight-records/trend`, { days })
}

export const createWeightRecord = (petId, weight, recordDate) => {
  return request.post(`/api/pets/${petId}/weight-records`, { weight, recordDate })
}

export const deleteWeightRecord = (petId, recordId) => {
  return request.delete(`/api/pets/${petId}/weight-records/${recordId}`)
}

export const recordStep = (steps, distance, recordDate, petId) => {
  return request.post('/api/health/steps', { steps, distance, record_date: recordDate, pet_id: petId })
}

export const recordWater = (amount, recordDate, recordTime, petId) => {
  return request.post('/api/health/water', { amount, record_date: recordDate, record_time: recordTime, pet_id: petId })
}

export const getHealthDashboard = (petId) => {
  return request.get('/api/health/dashboard', { pet_id: petId })
}

export const getHealthAnalysis = (petId) => {
  return request.post(`/api/health/analysis/${petId}`, {}, {}, 60000)
}

export const getAiModels = () => {
  return request.get('/api/health/ai-models')
}

export const getCurrentAiModel = () => {
  return request.get('/api/health/ai-models/current')
}

export const switchAiModel = (modelId) => {
  return request.post('/api/health/ai-models/switch', { modelId })
}
