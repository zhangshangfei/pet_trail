import request from '../../utils/request'

export const getMenuTree = () => request.get('/api/admin/menus/tree')
export const getMenuDetail = (id) => request.get(`/api/admin/menus/${id}`)
export const getUserMenus = () => request.get('/api/admin/menus/user')
export const createMenu = (data) => request.post('/api/admin/menus', data)
export const updateMenu = (id, data) => request.put(`/api/admin/menus/${id}`, data)
export const deleteMenu = (id) => request.delete(`/api/admin/menus/${id}`)
export const updateMenuStatus = (id, status) => request.put(`/api/admin/menus/${id}/status`, { status })
export const batchSortMenus = (data) => request.put('/api/admin/menus/batch-sort', data)
