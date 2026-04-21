import request from '../utils/request'

export const login = (data) => request.post('/api/admin/auth/login', data)
export const getProfile = () => request.get('/api/admin/auth/profile')
export const changePassword = (data) => request.put('/api/admin/admins/password', data)

export const getDashboardStats = () => request.get('/api/admin/dashboard/stats')
export const getTodayStats = () => request.get('/api/admin/dashboard/today')
export const getDashboardTrend = (days = 30) => request.get('/api/admin/dashboard/trend', { params: { days } })
export const getAuditStats = () => request.get('/api/admin/dashboard/audit-stats')

export const getUserList = (params) => request.get('/api/admin/users', { params })
export const getUserDetail = (id) => request.get(`/api/admin/users/${id}`)
export const getUserStats = (id) => request.get(`/api/admin/users/${id}/stats`)
export const updateUserStatus = (id, status) => request.put(`/api/admin/users/${id}/status`, { status })

export const getPetList = (params) => request.get('/api/admin/pets', { params })
export const getPetDetail = (id) => request.get(`/api/admin/pets/${id}`)
export const deletePet = (id) => request.delete(`/api/admin/pets/${id}`)

export const getPostList = (params) => request.get('/api/admin/posts', { params })
export const getPostDetail = (id) => request.get(`/api/admin/posts/${id}`)
export const auditPost = (id, data) => request.put(`/api/admin/posts/${id}/audit`, data)
export const deletePost = (id) => request.delete(`/api/admin/posts/${id}`)
export const batchAuditPosts = (data) => request.put('/api/admin/posts/batch-audit', data)
export const getDeletedPosts = (params) => request.get('/api/admin/posts/deleted', { params })
export const restorePost = (id) => request.put(`/api/admin/posts/${id}/restore`)

export const getCommentList = (params) => request.get('/api/admin/comments', { params })
export const deleteComment = (id) => request.delete(`/api/admin/comments/${id}`)
export const restoreComment = (id) => request.put(`/api/admin/comments/${id}/restore`)

export const getReportList = (params) => request.get('/api/admin/reports', { params })
export const handleReport = (id, data) => request.put(`/api/admin/reports/${id}/handle`, data)

export const getNotificationList = (params) => request.get('/api/admin/notifications', { params })
export const sendNotification = (data) => request.post('/api/admin/notifications', data)
export const broadcastNotification = (data) => request.post('/api/admin/notifications/broadcast', data)

export const getFeedbackList = (params) => request.get('/api/admin/feedbacks', { params })
export const getFeedbackDetail = (id) => request.get(`/api/admin/feedbacks/${id}`)
export const replyFeedback = (id, data) => request.put(`/api/admin/feedbacks/${id}/reply`, data)
export const updateFeedbackStatus = (id, status) => request.put(`/api/admin/feedbacks/${id}/status`, { status })
export const deleteFeedback = (id) => request.delete(`/api/admin/feedbacks/${id}`)

export const getAdminList = (params) => request.get('/api/admin/admins', { params })
export const createAdmin = (data) => request.post('/api/admin/admins', data)
export const updateAdmin = (id, data) => request.put(`/api/admin/admins/${id}`, data)
export const updateAdminStatus = (id, status) => request.put(`/api/admin/admins/${id}/status`, { status })
export const resetAdminPassword = (id) => request.put(`/api/admin/admins/${id}/reset-pwd`)

export const getOperationLogs = (params) => request.get('/api/admin/operation-logs', { params })

export const getSettings = () => request.get('/api/admin/settings')
export const updateSettings = (data) => request.put('/api/admin/settings', data)

export const exportUsers = (params) => request.get('/api/admin/export/users', { params, responseType: 'blob' })
export const exportPosts = (params) => request.get('/api/admin/export/posts', { params, responseType: 'blob' })
export const exportReports = (params) => request.get('/api/admin/export/reports', { params, responseType: 'blob' })
export const exportLogs = (params) => request.get('/api/admin/export/logs', { params, responseType: 'blob' })

export const getConfigList = (params) => request.get('/api/admin/config', { params })
export const getConfigCategories = () => request.get('/api/admin/config/categories')
export const createConfig = (data) => request.post('/api/admin/config', data)
export const updateConfig = (id, data) => request.put(`/api/admin/config/${id}`, data)
export const deleteConfig = (id) => request.delete(`/api/admin/config/${id}`)
export const refreshConfigCache = () => request.post('/api/admin/config/refresh-cache')
export const getConfigValue = (key) => request.get(`/api/admin/config/value/${key}`)

export const getAiModelList = () => request.get('/api/admin/ai-models')
export const getAiModelDashboard = () => request.get('/api/admin/ai-models/dashboard')
export const getAiModelDetail = (id) => request.get(`/api/admin/ai-models/${id}`)
export const getAiModelParameters = (id) => request.get(`/api/admin/ai-models/${id}/parameters`)
export const createAiModel = (data) => request.post('/api/admin/ai-models', data)
export const updateAiModel = (id, data) => request.put(`/api/admin/ai-models/${id}`, data)
export const updateAiModelParameters = (id, params) => request.put(`/api/admin/ai-models/${id}/parameters`, params)
export const setAiModelStatus = (id, status) => request.put(`/api/admin/ai-models/${id}/status`, { status })
export const switchAiModel = (data) => request.post('/api/admin/ai-models/switch', data)
export const deleteAiModel = (id) => request.delete(`/api/admin/ai-models/${id}`)
export const getAiModelSwitchLogs = (limit) => request.get('/api/admin/ai-models/switch-logs', { params: { limit } })
export const refreshAiModelCache = () => request.post('/api/admin/ai-models/refresh')
