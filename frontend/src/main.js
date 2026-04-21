import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import config from './config/env'

// 初始化微信云开发
// #ifdef MP-WEIXIN
if (wx.cloud) {
  wx.cloud.init({
    env: config.VITE_CLOUD_CONFIG.env,
    traceUser: true
  })
  console.log('微信云开发初始化完成，环境ID:', config.VITE_CLOUD_CONFIG.env)
}
// #endif

// 云托管配置
const CLOUD_CONFIG = config.VITE_CLOUD_CONFIG
const BASE_URL = config.VITE_API_BASE_URL

// 编码表单数据
function encodeFormData(data) {
  if (!data) return ''
  const params = []
  for (const key in data) {
    if (data.hasOwnProperty(key)) {
      const value = data[key]
      if (Array.isArray(value)) {
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

// 云托管请求
function cloudRequest(options = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    const header = {
      'X-WX-SERVICE': CLOUD_CONFIG.service
    }

    if (options.header && options.header['Content-Type']) {
      header['Content-Type'] = options.header['Content-Type']
    } else {
      header['Content-Type'] = 'application/json'
    }

    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    let requestData = options.data || {}
    if (header['Content-Type'] === 'application/x-www-form-urlencoded') {
      requestData = encodeFormData(requestData)
    }

    wx.cloud.callContainer({
      config: { env: CLOUD_CONFIG.env },
      path: options.url,
      header: header,
      method: options.method || 'GET',
      data: requestData,
      timeout: options.timeout || 20000,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data
          if (data.success) {
            resolve(data)
          } else {
            resolve(data)
          }
        } else if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('tokenExpireTime')
          uni.removeStorageSync('userInfo')
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/index' })
          }, 1500)
          reject(res)
        } else if (res.statusCode === 403) {
          reject(res)
        } else if (res.statusCode === 404) {
          uni.showToast({ title: '请求的资源不存在', icon: 'none', duration: 2000 })
          reject(res)
        } else if (res.statusCode >= 500) {
          uni.showToast({ title: '服务器开小差了', icon: 'none', duration: 2000 })
          reject(res)
        } else {
          reject(res)
        }
      },
      fail: (err) => {
        let errorMessage = '网络请求失败'
        if (err && err.errMsg && err.errMsg.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接'
        } else if (err && err.errMsg && err.errMsg.includes('fail')) {
          errorMessage = '网络连接失败，请检查网络'
        }
        uni.showToast({ title: errorMessage, icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

// HTTP 请求
function httpRequest(options = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    const header = {}

    if (options.header && options.header['Content-Type']) {
      header['Content-Type'] = options.header['Content-Type']
    } else {
      header['Content-Type'] = 'application/json'
    }

    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    let requestData = options.data || {}
    if (header['Content-Type'] === 'application/x-www-form-urlencoded') {
      requestData = encodeFormData(requestData)
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: requestData,
      header: header,
      timeout: options.timeout || 20000,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data
          if (data.success) {
            resolve(data)
          } else {
            resolve(data)
          }
        } else if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('tokenExpireTime')
          uni.removeStorageSync('userInfo')
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/index' })
          }, 1500)
          reject(res)
        } else if (res.statusCode === 403) {
          reject(res)
        } else if (res.statusCode === 404) {
          uni.showToast({ title: '请求的资源不存在', icon: 'none', duration: 2000 })
          reject(res)
        } else if (res.statusCode >= 500) {
          uni.showToast({ title: '服务器开小差了', icon: 'none', duration: 2000 })
          reject(res)
        } else {
          reject(res)
        }
      },
      fail: (err) => {
        let errorMessage = '网络请求失败'
        if (err && err.errMsg && err.errMsg.includes('timeout')) {
          errorMessage = '请求超时，请检查网络连接'
        } else if (err && err.errMsg && err.errMsg.includes('fail')) {
          errorMessage = '网络连接失败，请检查网络'
        }
        uni.showToast({ title: errorMessage, icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

// 统一请求函数
function request(options = {}) {
  if (BASE_URL === 'cloud') {
    return cloudRequest(options)
  }
  return httpRequest(options)
}

// 挂载到全局 uni 对象
uni.$request = {
  get(url, data = {}, header = {}) {
    return request({ url, method: 'GET', data, header })
  },
  post(url, data = {}, header = {}) {
    return request({ url, method: 'POST', data, header })
  },
  put(url, data = {}, header = {}) {
    return request({ url, method: 'PUT', data, header })
  },
  delete(url, data = {}, header = {}) {
    return request({ url, method: 'DELETE', data, header })
  }
}

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()

  app.use(pinia)

  return {
    app,
    pinia
  }
}