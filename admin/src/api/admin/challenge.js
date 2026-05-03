import request from '../../utils/request'

export const getChallengeList = (params) => request.get('/api/admin/challenges', { params })
export const getChallengeDetail = (id) => request.get(`/api/admin/challenges/${id}`)
export const createChallenge = (data) => request.post('/api/admin/challenges', data)
export const updateChallenge = (id, data) => request.put(`/api/admin/challenges/${id}`, data)
export const deleteChallenge = (id) => request.delete(`/api/admin/challenges/${id}`)
export const updateChallengeStatus = (id, status) => request.put(`/api/admin/challenges/${id}/status`, { status })
export const getChallengeStats = (id) => request.get(`/api/admin/challenges/${id}/stats`)
export const getChallengeParticipants = (id, params) => request.get(`/api/admin/challenges/${id}/participants`, { params })
