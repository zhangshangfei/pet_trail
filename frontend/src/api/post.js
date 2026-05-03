import request from '@/utils/request'

export const createPost = (data) => {
  return request.post('/api/posts', data)
}

export const getFeed = (page = 1, size = 20, tab = 'all') => {
  return request.get('/api/posts/feed', { page, size, tab })
}

export const getPostDetail = (id) => {
  return request.get(`/api/posts/${id}`)
}

export const toggleLike = (id) => {
  return request.post(`/api/posts/${id}/like`)
}

export const toggleEe = (id) => {
  return request.post(`/api/posts/${id}/ee`)
}

export const createComment = (postId, data) => {
  return request.post(`/api/posts/${postId}/comments`, data)
}

export const getComments = (postId, page = 1, size = 20) => {
  return request.get(`/api/posts/${postId}/comments`, { page, size })
}

export const getUserPosts = (userId, page = 1, size = 20) => {
  return request.get(`/api/posts/user/${userId}`, { page, size })
}

export const getUserLikedPosts = (userId, page = 1, size = 20) => {
  return request.get(`/api/posts/user/${userId}/likes`, { page, size })
}

export const sharePost = (id) => {
  return request.post(`/api/posts/${id}/share`)
}

export const deletePost = (id) => {
  return request.delete(`/api/posts/${id}`)
}

export const deleteComment = (postId, commentId) => {
  return request.delete(`/api/posts/${postId}/comments/${commentId}`)
}

export { toggleFollow, checkFollow, getFollowList, getFolloweeList, getFollowerList } from './follow'
