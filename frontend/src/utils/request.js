// API 基础地址配置
import config from '@/config/env'

const BASE_URL = config.VITE_API_BASE_URL
const CLOUD_CONFIG = config.VITE_CLOUD_CONFIG

const pendingRequests = new Map()
const responseCache = new Map()
const CACHE_TTL = 3000
const DEDUP_TTL = 500

// 将对象编码为 application/x-www-form-urlencoded 格式
const encodeFormData = (data) => {
  if (!data) return ''
  const params = []
  for (const key in data) {
    if (data.hasOwnProperty(key)) {
      const value = data[key]
      if (Array.isArray(value)) {
        // 数组参数
        value.forEach(v => {
          params.push(encodeURIComponent(key) + '=' + encodeURIComponent(v))
        })
      } else {
        params.push(encodeURIComponent(key) + '=' + encodeURIComponent(value))
      }
    }
  }
  return params.join('&')
}

// 云托管请求封装
const cloudRequest = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 获取 Token
    const token = uni.getStorageSync('token') || ''

    // 构建请求头
    const header = {
      'X-WX-SERVICE': CLOUD_CONFIG.service
    }
    
    // 处理 Content-Type
    if (options.header && options.header['Content-Type']) {
      header['Content-Type'] = options.header['Content-Type']
    } else {
      header['Content-Type'] = 'application/json'
    }

    // 如果有 Token，添加到请求头
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    // 处理数据编码
    let requestData = options.data || {}
    if (header['Content-Type'] === 'application/x-www-form-urlencoded') {
      requestData = encodeFormData(requestData)
    }

    // 发起云托管请求
    wx.cloud.callContainer({
      config: {
        env: CLOUD_CONFIG.env
      },
      path: options.url,
      header: header,
      method: options.method || 'GET',
      data: requestData,
      timeout: options.timeout || 20000,
      success: (res) => {
        // 检查 HTTP 状态码
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data

          // 检查业务状态
          if (data.success) {
            resolve(data)
          } else {
            // 业务失败
            const message = data.message || '请求失败'
            uni.showToast({
              title: message,
              icon: 'none',
              duration: 2000
            })
            reject(data)
          }
        } else if (res.statusCode === 401) {
          // Token 无效或过期
          console.warn('Token 无效或已过期，清除本地存储')
          uni.removeStorageSync('token')
          uni.removeStorageSync('tokenExpireTime')
          uni.removeStorageSync('userInfo')

          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 2000
          })

          // 延迟跳转到登录页
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/index'
            })
          }, 1500)

          reject(res)
        } else if (res.statusCode === 403) {
          reject(res)
        } else if (res.statusCode === 404) {
          // 资源不存在
          uni.showToast({
            title: '请求的资源不存在',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else if (res.statusCode >= 500) {
          uni.showToast({
            title: '服务器开小差了',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else {
          reject(res)
        }
      },
      fail: (err) => {
        console.error('云托管请求失败:', err)

        let errorMessage = '网络请求失败'
        if (err && err.errMsg && err.errMsg.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接'
        } else if (err && err.errMsg && err.errMsg.includes('fail')) {
          errorMessage = '网络连接失败，请检查网络'
        }

        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 2000
        })
        reject(err)
      }
    })
  })
}

// 普通 HTTP 请求封装
const httpRequest = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 获取 Token
    const token = uni.getStorageSync('token') || ''

    // 构建请求头 - 先设置默认值，再合并用户传入的 header
    const header = {}
    if (options.header && options.header['Content-Type']) {
      header['Content-Type'] = options.header['Content-Type']
    } else {
      header['Content-Type'] = 'application/json'
    }

    // 如果有 Token，添加到请求头
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    // 处理数据编码
    let requestData = options.data || {}
    if (header['Content-Type'] === 'application/x-www-form-urlencoded') {
      requestData = encodeFormData(requestData)
    }

    // 发起请求
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: requestData,
      header: header,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data
          if (data.success) {
            resolve(data)
          } else {
            reject(data)
          }
        } else if (res.statusCode === 401) {
          // Token 无效或过期 - 由后端校验
          console.warn('Token 无效或已过期，清除本地存储')
          uni.removeStorageSync('token')
          uni.removeStorageSync('tokenExpireTime')
          uni.removeStorageSync('userInfo')

          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 2000
          })

          // 延迟跳转到登录页
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/index'
            })
          }, 1500)

          reject(res)
        } else if (res.statusCode === 403) {
          reject(res)
        } else if (res.statusCode === 404) {
          uni.showToast({
            title: '请求的资源不存在',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else if (res.statusCode >= 500) {
          uni.showToast({
            title: '服务器开小差了',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else {
          reject(res)
        }
      },
      fail: (err) => {
        console.error('网络请求失败:', err)

        let errorMessage = '网络请求失败'
        if (err && err.errMsg && err.errMsg.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接'
        } else if (err && err.errMsg && err.errMsg.includes('fail')) {
          errorMessage = '网络连接失败，请检查网络'
        }

        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 2000
        })
        reject(err)
      }
    })
  })
}

// 将对象编码为 URL 查询字符串
const buildQueryString = (data) => {
  if (!data || typeof data !== 'object') return ''
  const params = []
  for (const key in data) {
    if (data.hasOwnProperty(key)) {
      const value = data[key]
      if (value !== undefined && value !== null && value !== '' && value !== 'undefined' && value !== 'null') {
        params.push(encodeURIComponent(key) + '=' + encodeURIComponent(value))
      }
    }
  }
  return params.length > 0 ? '?' + params.join('&') : ''
}

// 请求拦截器 - 根据环境选择请求方式
const request = (options = {}) => {
  if (options.method === 'GET' && options.data && typeof options.data === 'object') {
    const queryString = buildQueryString(options.data)
    if (queryString) {
      options.url = options.url + queryString
      options.data = {}
    }
  }

  if (options.method === 'GET') {
    const cacheKey = options.url
    const now = Date.now()

    const cached = responseCache.get(cacheKey)
    if (cached && now - cached.timestamp < CACHE_TTL) {
      return Promise.resolve(cached.data)
    }

    const pending = pendingRequests.get(cacheKey)
    if (pending && now - pending.timestamp < DEDUP_TTL) {
      return pending.promise
    }

    const promise = (BASE_URL === 'cloud' ? cloudRequest(options) : httpRequest(options))
      .then(data => {
        responseCache.set(cacheKey, { data, timestamp: Date.now() })
        pendingRequests.delete(cacheKey)
        return data
      })
      .catch(err => {
        pendingRequests.delete(cacheKey)
        throw err
      })

    pendingRequests.set(cacheKey, { promise, timestamp: now })
    return promise
  }
  
  if (BASE_URL === 'cloud') {
    return cloudRequest(options)
  }
  return httpRequest(options)
}

// 封装常用请求方法
export default {
  // GET 请求
  get(url, data = {}, header = {}) {
    return request({
      url,
      method: 'GET',
      data,
      header
    })
  },

  // POST 请求
  post(url, data = {}, header = {}, timeout) {
    return request({
      url,
      method: 'POST',
      data,
      header,
      timeout
    })
  },

  // PUT 请求
  put(url, data = {}, header = {}) {
    return request({
      url,
      method: 'PUT',
      data,
      header
    })
  },

  // DELETE 请求
  delete(url, data = {}, header = {}) {
    return request({
      url,
      method: 'DELETE',
      data,
      header
    })
  }
}
