import request from '@/utils/request'

export const getChallenges = () => {
  return request.get('/api/challenges')
}

export const getChallengeDetail = (id) => {
  return request.get(`/api/challenges/${id}`)
}

export const joinChallenge = (id) => {
  return request.post(`/api/challenges/${id}/join`)
}

export const getMyChallenges = () => {
  return request.get('/api/challenges/my')
}

export const updateChallengeProgress = (id) => {
  return request.post(`/api/challenges/${id}/progress`)
}

export const getChallengeLeaderboard = (id, limit = 50) => {
  return request.get(`/api/challenges/${id}/leaderboard`, { limit })
}
