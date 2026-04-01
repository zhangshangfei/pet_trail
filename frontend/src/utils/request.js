// API 基础地址配置
import config from '@/config/env'

const BASE_URL = config.VITE_API_BASE_URL

// Token 过期时间（毫秒）
const TOKEN_EXPIRED_TIME = 7 * 24 * 60 * 60 * 1000 // 7 天

// 请求拦截器
const request = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 获取 Token
    const token = uni.getStorageSync('token') || ''
    const tokenExpireTime = uni.getStorageSync('tokenExpireTime') || 0

    // 检查 Token 是否过期
    if (token && Date.now() > tokenExpireTime) {
      console.warn('Token 已过期，请重新登录')
      uni.removeStorageSync('token')
      uni.removeStorageSync('tokenExpireTime')
      uni.removeStorageSync('userInfo')
      
      // 跳转到登录页（如果存在）
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      if (currentPage && currentPage.route !== 'pages/login/index') {
        uni.reLaunch({
          url: '/pages/login/index'
        })
      }
      
      reject({ statusCode: 401, data: { message: 'Token 已过期' } })
      return
    }

    // 构建请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }

    // 如果有 Token，添加到请求头
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    // 发起请求
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        console.log('请求响应:', res)

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
          console.warn('Token 无效，清除本地存储')
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
