import request from '@/utils/request'

/**
 * 用户注册
 * @param {string} openid - 微信 OpenID
 * @param {string} unionid - 微信 UnionID（可选）
 * @param {string} nickname - 用户昵称（可选）
 * @param {string} avatar - 用户头像（可选）
 */
export const register = (openid, unionid = '', nickname = '', avatar = '') => {
  return request.post('/api/users/register', null, {
    params: {
      openid,
      unionid,
      nickname,
      avatar
    }
  })
}

/**
 * 用户登录
 * @param {string} openid - 微信 OpenID
 */
export const login = (openid) => {
  return request.post('/api/users/login', null, {
    params: {
      openid
    }
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
  return request.put('/api/users/profile', null, {
    params: data
  })
}
