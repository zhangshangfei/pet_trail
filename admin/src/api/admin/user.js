import request from '../../utils/request'

export const getUserList = (params) => request.get('/api/admin/users', { params })
export const getUserDetail = (id) => request.get(`/api/admin/users/${id}`)
export const getUserStats = (id) => request.get(`/api/admin/users/${id}/stats`)
export const updateUserStatus = (id, status) => request.put(`/api/admin/users/${id}/status`, { status })
