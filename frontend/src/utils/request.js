// API 基础地址配置
import config from '@/config/env'

const BASE_URL = config.VITE_API_BASE_URL
const CLOUD_CONFIG = config.VITE_CLOUD_CONFIG

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
          // 无权限
          uni.showToast({
            title: '无权限访问',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else if (res.statusCode === 404) {
          // 资源不存在
          uni.showToast({
            title: '请求的资源不存在',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else {
          // 其他 HTTP 错误
          uni.showToast({
            title: `请求失败：${res.statusCode}`,
            icon: 'none',
            duration: 2000
          })
          reject(res)
        }
      },
      fail: (err) => {
        console.error('云托管请求失败:', err)

        // 判断错误类型
        let errorMessage = '网络请求失败'
        if (err.errMsg && err.errMsg.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接'
        } else if (err.errMsg && err.errMsg.includes('fail')) {
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
          // 无权限
          uni.showToast({
            title: '无权限访问',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else if (res.statusCode === 404) {
          // 资源不存在
          uni.showToast({
            title: '请求的资源不存在',
            icon: 'none',
            duration: 2000
          })
          reject(res)
        } else {
          const errorMessage = (res.data && res.data.message) || `请求失败：${res.statusCode}`;
          uni.showToast({
            title: errorMessage,
            icon: 'none',
            duration: 3000
          })
          reject(res)
        }
      },
      fail: (err) => {
        console.error('网络请求失败:', err)

        // 判断错误类型
        let errorMessage = '网络请求失败'
        if (err.errMsg && err.errMsg.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接'
        } else if (err.errMsg && err.errMsg.includes('fail')) {
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
    if (data.hasOwnProperty(key) && data[key] !== undefined && data[key] !== null) {
      params.push(encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
    }
  }
  return params.length > 0 ? '?' + params.join('&') : ''
}

// 请求拦截器 - 根据环境选择请求方式
const request = (options = {}) => {
  // GET 请求时，将 data 参数拼接到 URL 查询字符串中
  if (options.method === 'GET' && options.data && typeof options.data === 'object') {
    const queryString = buildQueryString(options.data)
    if (queryString) {
      options.url = options.url + queryString
      options.data = {}
    }
  }
  
  // 如果 BASE_URL 为 'cloud'，使用云托管请求
  if (BASE_URL === 'cloud') {
    return cloudRequest(options)
  }
  // 否则使用普通 HTTP 请求
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
  post(url, data = {}, header = {}) {
    return request({
      url,
      method: 'POST',
      data,
      header
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
