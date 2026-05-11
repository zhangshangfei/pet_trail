import request from '../../utils/request'

export const getAdminList = (params) => request.get('/api/admin/admins', { params })
export const getAdminDetail = (id) => request.get(`/api/admin/admins/${id}`)
export const createAdmin = (data) => request.post('/api/admin/admins', data)
export const updateAdmin = (id, data) => request.put(`/api/admin/admins/${id}`, data)
export const updateAdminStatus = (id, status) => request.put(`/api/admin/admins/${id}/status`, { status })
export const resetAdminPassword = (id, password) => request.put(`/api/admin/admins/${id}/reset-password`, { password })
export const changeAdminPassword = (id, oldPassword, newPassword) => request.put(`/api/admin/admins/${id}/change-password`, { oldPassword, newPassword })
export const bindTotp = (id) => request.post(`/api/admin/admins/${id}/totp/bind`)
export const unbindTotp = (id, code) => request.put(`/api/admin/admins/${id}/totp/unbind`, { code })
export const verifyTotp = (id, code) => request.post(`/api/admin/admins/${id}/totp/verify`, { code })
