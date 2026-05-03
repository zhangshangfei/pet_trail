import request from '../../utils/request'

export const getSettings = () => request.get('/api/admin/settings')
export const updateSettings = (data) => request.put('/api/admin/settings', data)
