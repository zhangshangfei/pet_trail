import request from '@/utils/request'

/**
 * 发布动态
 */
export const createPost = (data) => {
  // 直接发送 JSON 格式
  return request.post('/api/posts', data)
}

/**
 * 获取动态列表
 */
export const getFeed = (page = 1, size = 20) => {
  return request.get('/api/posts/feed', {
    params: { page, size }
  })
}

/**
 * 获取动态详情
 */
export const getPostDetail = (id) => {
  return request.get(`/api/posts/${id}`)
}

/**
 * 点赞/取消点赞
 */
export const toggleLike = (id) => {
  return request.post(`/api/posts/${id}/like`)
}

/**
 * 发布评论
 */
export const createComment = (postId, data) => {
  return request.post(`/api/posts/${postId}/comments`, data)
}

/**
 * 获取评论列表
 */
export const getComments = (postId, page = 1, size = 20) => {
  return request.get(`/api/posts/${postId}/comments`, {
    params: { page, size }
  })
}
