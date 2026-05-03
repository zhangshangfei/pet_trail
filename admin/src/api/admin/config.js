import request from '../../utils/request'

export const getConfigList = (params) => request.get('/api/admin/config', { params })
export const getConfigDetail = (id) => request.get(`/api/admin/config/${id}`)
export const getConfigCategories = () => request.get('/api/admin/config/categories')
export const createConfig = (data) => request.post('/api/admin/config', data)
export const updateConfig = (id, data) => request.put(`/api/admin/config/${id}`, data)
export const deleteConfig = (id) => request.delete(`/api/admin/config/${id}`)
export const refreshConfigCache = () => request.post('/api/admin/config/refresh-cache')
export const getConfigValue = (key) => request.get(`/api/admin/config/value/${key}`)
