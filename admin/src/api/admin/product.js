import request from '../../utils/request'

export const getProductList = (params) => request.get('/api/admin/products', { params })
export const getProductDetail = (id) => request.get(`/api/admin/products/${id}`)
export const createProduct = (data) => request.post('/api/admin/products', data)
export const updateProduct = (id, data) => request.put(`/api/admin/products/${id}`, data)
export const deleteProduct = (id) => request.delete(`/api/admin/products/${id}`)
export const updateProductStatus = (id, status) => request.put(`/api/admin/products/${id}/status`, { status })
export const getProductStats = () => request.get('/api/admin/products/stats')
export const getOrderList = (params) => request.get('/api/admin/products/orders', { params })
