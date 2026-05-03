import request from '../../utils/request'

export const getPostList = (params) => request.get('/api/admin/posts', { params })
export const getPostDetail = (id) => request.get(`/api/admin/posts/${id}`)
export const auditPost = (id, data) => request.put(`/api/admin/posts/${id}/audit`, data)
export const deletePost = (id) => request.delete(`/api/admin/posts/${id}`)
export const batchAuditPosts = (data) => request.put('/api/admin/posts/batch-audit', data)
export const getDeletedPosts = (params) => request.get('/api/admin/posts/deleted', { params })
export const restorePost = (id) => request.put(`/api/admin/posts/${id}/restore`)
