import request from '@/utils/request'

/**
 * 微信登录（使用 code 换取 token）
 * @param {string} code - 微信登录 code
 */
export const loginByCode = (code) => {
  return request.post('/api/users/login', { code })
}

/**
 * 用户注册（备用接口）
 * @param {string} openid - 微信 OpenID
 * @param {string} unionid - 微信 UnionID（可选）
 * @param {string} nickname - 用户昵称（可选）
 * @param {string} avatar - 用户头像（可选）
 */
export const register = (openid, unionid = '', nickname = '', avatar = '') => {
  return request.post('/api/users/register', {
    openid,
    unionid,
    nickname,
    avatar
  })
}

/**
 * 获取用户资料
 */
export const getProfile = () => {
  return request.get('/api/users/profile')
}

/**
 * 更新用户资料
 * @param {object} data - 用户资料数据
 * @param {string} data.nickname - 用户昵称
 * @param {string} data.avatar - 用户头像
 * @param {string} data.phone - 手机号
 * @param {number} data.gender - 性别（1-男，2-女）
 */
export const updateProfile = (data) => {
  return request.put('/api/users/profile', data)
}

export const getUserById = (userId) => {
  return request.get(`/api/users/${userId}`)
}

export const discoverUsers = (params) => {
  return request.get('/api/users/discover', params)
}

/**
 * 退出登录
 */
export const logout = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('tokenExpireTime')
  uni.removeStorageSync('userInfo')
  return Promise.resolve()
}

export const deleteAccount = () => {
  return request.delete('/api/users/account')
}
