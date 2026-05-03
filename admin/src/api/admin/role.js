import request from '../../utils/request'

export const getRoleList = (params) => request.get('/api/admin/roles', { params })
export const getAllRoles = () => request.get('/api/admin/roles/all')
export const getRoleDetail = (id) => request.get(`/api/admin/roles/${id}`)
export const createRole = (data) => request.post('/api/admin/roles', data)
export const updateRole = (id, data) => request.put(`/api/admin/roles/${id}`, data)
export const deleteRole = (id) => request.delete(`/api/admin/roles/${id}`)
export const updateRoleStatus = (id, status) => request.put(`/api/admin/roles/${id}/status`, { status })
export const getRoleMenus = (id) => request.get(`/api/admin/roles/${id}/menus`)
export const saveRoleMenus = (id, data) => request.put(`/api/admin/roles/${id}/menus`, data)
