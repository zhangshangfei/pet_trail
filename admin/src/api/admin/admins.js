import request from '../../utils/request'

export const getAdminList = (params) => request.get('/api/admin/admins', { params })
export const getAdminDetail = (id) => request.get(`/api/admin/admins/${id}`)
export const createAdmin = (data) => request.post('/api/admin/admins', data)
export const updateAdmin = (id, data) => request.put(`/api/admin/admins/${id}`, data)
export const updateAdminStatus = (id, status) => request.put(`/api/admin/admins/${id}/status`, { status })
export const resetAdminPassword = (id) => request.put(`/api/admin/admins/${id}/reset-pwd`)
