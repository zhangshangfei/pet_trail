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
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @param {string} tab - tab类型：all-全部, follow-关注, recommend-推荐
 */
export const getFeed = (page = 1, size = 20, tab = 'all') => {
  return request.get('/api/posts/feed', { page, size, tab })
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
 * 收藏/取消收藏
 */
export const toggleEe = (id) => {
  return request.post(`/api/posts/${id}/ee`)
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
  return request.get(`/api/posts/${postId}/comments`, { page, size })
}

/**
 * 关注/取消关注
 */
export const toggleFollow = (followeeId) => {
  return request.post(`/api/follows/${followeeId}`)
}

/**
 * 检查是否已关注
 */
export const checkFollow = (followeeId) => {
  return request.get(`/api/follows/check/${followeeId}`)
}

/**
 * 获取关注列表
 */
export const getFollowList = () => {
  return request.get('/api/follows/list')
}

export const getFolloweeList = (userId, page = 1, size = 20) => {
  return request.get('/api/follows/followees', { userId, page, size })
}

export const getFollowerList = (userId, page = 1, size = 20) => {
  return request.get('/api/follows/followers', { userId, page, size })
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
