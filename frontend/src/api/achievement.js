import request from '@/utils/request'

export const getAchievements = (type) => {
  return request.get('/api/achievements', { type })
}

export const getAchievementDetail = (id) => {
  return request.get(`/api/achievements/${id}`)
}

export const claimAchievement = (id) => {
  return request.post(`/api/achievements/${id}/claim`)
}

export const checkAchievements = (conditionType) => {
  return request.post('/api/achievements/check', { conditionType })
}
