import request from '../../utils/request'

export const login = (data) => request.post('/api/admin/auth/login', data)
export const getProfile = () => request.get('/api/admin/auth/profile')
export const getAllPermissions = () => request.get('/api/admin/auth/permissions')
export const changePassword = (data) => request.put('/api/admin/admins/password', data)
