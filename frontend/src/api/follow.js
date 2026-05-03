import request from '@/utils/request'

export const toggleFollow = (followeeId) => {
  return request.post(`/api/follows/${followeeId}`)
}

export const checkFollow = (followeeId) => {
  return request.get(`/api/follows/check/${followeeId}`)
}

export const getFollowList = () => {
  return request.get('/api/follows/list')
}

export const getFolloweeList = (userId, page = 1, size = 20) => {
  return request.get('/api/follows/followees', { userId, page, size })
}

export const getFollowerList = (userId, page = 1, size = 20) => {
  return request.get('/api/follows/followers', { userId, page, size })
}
