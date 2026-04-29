import request from '@/utils/request'

export const getHotTags = (limit = 20) => {
  return request.get('/api/tags/hot', { limit })
}

export const searchTags = (keyword, limit = 20) => {
  return request.get('/api/tags/search', { keyword, limit })
}

export const getTagPosts = (tagId, page = 1, size = 20) => {
  return request.get(`/api/tags/${tagId}/posts`, { page, size })
}

export const getTagByName = (name) => {
  return request.get(`/api/tags/name/${encodeURIComponent(name)}`)
}

export const getTagPostsByName = (name, page = 1, size = 20) => {
  return request.get(`/api/tags/name/${encodeURIComponent(name)}/posts`, { page, size })
}
