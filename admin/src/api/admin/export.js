import request from '../../utils/request'

export const exportUsers = (params) => request.get('/api/admin/export/users', { params, responseType: 'blob' })
export const exportPosts = (params) => request.get('/api/admin/export/posts', { params, responseType: 'blob' })
export const exportReports = (params) => request.get('/api/admin/export/reports', { params, responseType: 'blob' })
export const exportLogs = (params) => request.get('/api/admin/export/logs', { params, responseType: 'blob' })
export const exportPets = (params) => request.get('/api/admin/export/pets', { params, responseType: 'blob' })
export const exportChallenges = (params) => request.get('/api/admin/export/challenges', { params, responseType: 'blob' })
export const exportProducts = (params) => request.get('/api/admin/export/products', { params, responseType: 'blob' })
