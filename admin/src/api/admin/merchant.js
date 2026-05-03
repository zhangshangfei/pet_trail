import request from '../../utils/request'

export const getMerchantList = (params) => request.get('/api/admin/merchants', { params })
export const getMerchantDetail = (id) => request.get(`/api/admin/merchants/${id}`)
export const createMerchant = (data) => request.post('/api/admin/merchants', data)
export const updateMerchant = (id, data) => request.put(`/api/admin/merchants/${id}`, data)
export const updateMerchantStatus = (id, status) => request.put(`/api/admin/merchants/${id}/status`, { status })
export const deleteMerchant = (id) => request.delete(`/api/admin/merchants/${id}`)
