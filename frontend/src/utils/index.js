/**
 * 通用工具函数
 */

/**
 * 格式化日期
 * @param {string|Date} date - 日期字符串或 Date 对象
 * @param {string} format - 格式模板，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return '-'
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 获取性别文本
 * @param {number} gender - 性别代码 (0-未知，1-公/男，2-母/女)
 * @returns {string} 性别文本
 */
export const getGenderText = (gender) => {
  const map = {
    0: '未知',
    1: '公',
    2: '母'
  }
  return map[gender] ?? '未知'
}

/**
 * 获取用户性别文本
 * @param {number} gender - 性别代码 (0-未知，1-男，2-女)
 * @returns {string} 性别文本
 */
export const getUserGenderText = (gender) => {
  const map = {
    0: '未知',
    1: '男',
    2: '女'
  }
  return map[gender] ?? '未知'
}

/**
 * 获取宠物类别文本
 * @param {number} category - 类别代码 (0-其他，1-猫，2-狗)
 * @returns {string} 类别文本
 */
export const getPetCategoryText = (category) => {
  const map = {
    0: '其他',
    1: '猫',
    2: '狗'
  }
  return map[category] ?? '其他'
}

/**
 * 计算距离今天的天数
 * @param {string|Date} dateStr - 目标日期
 * @returns {number} 距离天数，负数表示已过期
 */
export const daysUntil = (dateStr) => {
  if (!dateStr) return 0
  const target = new Date(dateStr)
  const now = new Date()
  const diff = Math.ceil((target - now) / (1000 * 60 * 60 * 24))
  return diff
}

/**
 * 计算体重变化百分比
 * @param {number} oldWeight - 原体重
 * @param {number} newWeight - 新体重
 * @returns {string} 变化百分比，保留 1 位小数
 */
export const calculateWeightChange = (oldWeight, newWeight) => {
  if (!oldWeight || !newWeight) return '0.0'
  const change = ((newWeight - oldWeight) / oldWeight) * 100
  return change.toFixed(1)
}

/**
 * 格式化体重变化文本
 * @param {number} changePercent - 变化百分比
 * @returns {object} { text, type } text: 显示文本，type: up/down/same
 */
export const formatWeightChange = (changePercent) => {
  const value = parseFloat(changePercent)
  if (isNaN(value)) return { text: '-', type: 'same' }
  
  if (value > 0) {
    return { text: `+${value}%`, type: 'up' }
  } else if (value < 0) {
    return { text: `${value}%`, type: 'down' }
  } else {
    return { text: '0%', type: 'same' }
  }
}

/**
 * 防抖函数
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export const debounce = (fn, delay = 300) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn - 需要节流的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export const throttle = (fn, delay = 300) => {
  let lastTime = 0
  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      fn.apply(this, args)
      lastTime = now
    }
  }
}

/**
 * 深拷贝
 * @param {*} obj - 需要拷贝的对象
 * @returns {*} 拷贝后的对象
 */
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj)
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  
  const clonedObj = {}
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      clonedObj[key] = deepClone(obj[key])
    }
  }
  return clonedObj
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 验证手机号（中国大陆）
 * @param {string} phone - 手机号
 * @returns {boolean} 是否有效
 */
export const isValidPhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否有效
 */
export const isValidEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export const loadWxSubscribeTemplates = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) {
      console.warn('loadWxSubscribeTemplates: 未登录，跳过')
      return
    }
    const res = await uni.$request.get('/api/wx-subscribe/templates')
    console.log('订阅消息模板接口返回:', JSON.stringify(res))
    if (res && res.success && res.data) {
      const templates = res.data
      for (const [key, value] of Object.entries(templates)) {
        if (value && value.trim()) {
          uni.setStorageSync('wxSubscribeTemplate_' + key, value.trim())
          console.log('缓存模板ID: wxSubscribeTemplate_' + key + ' = ' + value.trim())
        } else {
          console.warn('模板ID为空，跳过: ' + key)
        }
      }
    } else {
      console.warn('获取模板ID接口返回异常:', res)
    }
  } catch (e) {
    console.warn('获取订阅消息模板ID失败:', e)
  }
}

export const requestWxSubscribe = async (types) => {
  if (!types || !types.length) return
  const tmplIds = []
  const typeToTmplId = {}
  for (const t of types) {
    let id = uni.getStorageSync('wxSubscribeTemplate_' + t)
    if (!id || !id.trim()) {
      await loadWxSubscribeTemplates()
      id = uni.getStorageSync('wxSubscribeTemplate_' + t)
    }
    if (id && id.trim()) {
      tmplIds.push(id.trim())
      typeToTmplId[id.trim()] = t
    }
  }
  if (!tmplIds.length) {
    console.warn('订阅消息模板ID未配置，跳过授权请求。types=', types)
    return
  }
  console.log('请求订阅消息授权, tmplIds=', tmplIds)
  uni.requestSubscribeMessage({
    tmplIds,
    success: (res) => {
      console.log('订阅消息授权结果:', res)
      for (const [tmplId, result] of Object.entries(res)) {
        if (result === 'accept') {
          const templateType = typeToTmplId[tmplId]
          if (templateType) {
            reportAuthorization(templateType, 1)
          }
        }
      }
    },
    fail: (err) => {
      console.warn('订阅消息授权失败:', err)
    }
  })
}

const reportAuthorization = async (templateType, count) => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) return
    await uni.$request.post('/api/wx-subscribe/authorize', { templateType, count })
    console.log('授权积分上报成功:', templateType, '+', count)
  } catch (e) {
    console.warn('授权积分上报失败:', e)
  }
}

export const wechatLogin = () => {
  return new Promise((resolve) => {
    uni.showLoading({ title: '登录中...', mask: true })
    wx.login({
      success: async (res) => {
        if (res.code) {
          try {
            const loginRes = await uni.$request.post('/api/users/login', { code: res.code })
            uni.hideLoading()
            if (loginRes.success) {
              uni.setStorageSync('token', loginRes.data.token)
              uni.setStorageSync('tokenExpireTime', Date.now() + 7 * 24 * 60 * 60 * 1000)
              uni.setStorageSync('userInfo', loginRes.data.user)
              uni.showToast({ title: '登录成功', icon: 'success' })
              uni.$emit('loginSuccess')
              loadWxSubscribeTemplates()
              resolve(true)
            } else {
              uni.showToast({ title: loginRes.message || '登录失败', icon: 'none' })
              resolve(false)
            }
          } catch (err) {
            uni.hideLoading()
            uni.showToast({ title: '网络错误', icon: 'none' })
            resolve(false)
          }
        } else {
          uni.hideLoading()
          uni.showToast({ title: '获取登录凭证失败', icon: 'none' })
          resolve(false)
        }
      },
      fail: () => {
        uni.hideLoading()
        uni.showToast({ title: '登录失败，请重试', icon: 'none' })
        resolve(false)
      }
    })
  })
}

const DEFAULT_AVATAR_COLORS = [
  '#FF7A3D', '#FF4D6A', '#6C5CE7', '#00B894', '#0984E3', '#E17055', '#FDCB6E', '#A29BFE',
  '#00CEC9', '#E84393', '#55A3E8', '#F39C12', '#2ECC71', '#9B59B6', '#1ABC9C', '#E74C3C'
]

function simpleHash(str) {
  let hash = 0
  const s = String(str || 0)
  for (let i = 0; i < s.length; i++) {
    hash = ((hash << 5) - hash) + s.charCodeAt(i)
    hash = hash & hash
  }
  return Math.abs(hash)
}

export function getFirstChar(name) {
  if (!name || !name.trim()) return '?'
  const trimmed = name.trim()
  const first = trimmed.charAt(0)
  if (/[\u4e00-\u9fff]/.test(first)) return first
  if (/[a-zA-Z]/.test(first)) return first.toUpperCase()
  if (/[0-9]/.test(first)) return first
  return '?'
}

export function getAvatarColor(id) {
  const colorIndex = simpleHash(String(id || 0)) % DEFAULT_AVATAR_COLORS.length
  return DEFAULT_AVATAR_COLORS[colorIndex]
}

export function generateDefaultAvatar(name, id) {
  const colorIndex = simpleHash(String(id || name || 0)) % DEFAULT_AVATAR_COLORS.length
  const bgColor = DEFAULT_AVATAR_COLORS[colorIndex]
  const char = getFirstChar(name)

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect width="200" height="200" rx="100" fill="${bgColor}"/><text x="100" y="108" font-size="80" font-family="PingFang SC,Microsoft YaHei,sans-serif" font-weight="600" fill="white" text-anchor="middle" dominant-baseline="middle">${char}</text></svg>`

  return 'data:image/svg+xml,' + encodeURIComponent(svg)
}

export const DEFAULT_USER_AVATAR = ''
export const DEFAULT_PET_AVATAR_URL = ''

export function getUserAvatar(userId, avatarUrl) {
  if (avatarUrl && avatarUrl.startsWith('http')) {
    return avatarUrl
  }
  return ''
}

export function getPetAvatar(petId, avatarUrl) {
  if (avatarUrl && avatarUrl.startsWith('http')) {
    return avatarUrl
  }
  return ''
}

export function isDefaultAvatar(url) {
  return !url || url === '' || url.startsWith('data:image/svg+xml')
}

export const checkLogin = (content = '请先登录后再操作') => {
  return new Promise((resolve) => {
    const token = uni.getStorageSync('token')
    if (token) {
      resolve(true)
      return
    }
    uni.showModal({
      title: '提示',
      content,
      showCancel: true,
      confirmText: '去登录',
      success: async (res) => {
        if (res.confirm) {
          const loginResult = await wechatLogin()
          resolve(loginResult)
        } else {
          resolve(false)
        }
      }
    })
  })
}
