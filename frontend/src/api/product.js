import request from '@/utils/request'

export const getProducts = (params) => {
  return request.get('/api/products', params)
}

export const getProductDetail = (id) => {
  return request.get(`/api/products/${id}`)
}

export const getRecommendProducts = (limit = 10) => {
  return request.get('/api/products/recommend', { limit })
}

export const getProductCategories = () => {
  return request.get('/api/products/categories')
}
