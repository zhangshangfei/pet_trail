import request from '../../utils/request'

export const getFeedbackList = (params) => request.get('/api/admin/feedbacks', { params })
export const getFeedbackDetail = (id) => request.get(`/api/admin/feedbacks/${id}`)
export const replyFeedback = (id, data) => request.put(`/api/admin/feedbacks/${id}/reply`, data)
export const updateFeedbackStatus = (id, data) => request.put(`/api/admin/feedbacks/${id}/status`, data)
export const deleteFeedback = (id) => request.delete(`/api/admin/feedbacks/${id}`)
