import request from '../../utils/request'

export const getCommentList = (params) => request.get('/api/admin/comments', { params })
export const deleteComment = (id) => request.delete(`/api/admin/comments/${id}`)
export const restoreComment = (id) => request.put(`/api/admin/comments/${id}/restore`)
