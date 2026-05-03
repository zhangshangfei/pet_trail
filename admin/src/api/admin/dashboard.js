import request from '../../utils/request'

export const getDashboardStats = () => request.get('/api/admin/dashboard/stats')
export const getTodayStats = () => request.get('/api/admin/dashboard/today')
export const getDashboardTrend = (days = 30) => request.get('/api/admin/dashboard/trend', { params: { days } })
export const getAuditStats = () => request.get('/api/admin/dashboard/audit-stats')
