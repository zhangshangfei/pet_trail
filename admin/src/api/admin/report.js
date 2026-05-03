import request from '../../utils/request'

export const getReportList = (params) => request.get('/api/admin/reports', { params })
export const handleReport = (id, data) => request.put(`/api/admin/reports/${id}/handle`, data)
