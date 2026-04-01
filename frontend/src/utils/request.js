// API 基础地址配置
const BASE_URL = 'http://localhost:8080'

// 请求拦截器
const request = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 获取 Token
    const token = uni.getStorageSync('token') || ''

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
        console.log('请求成功:', res)

        // 检查响应状态
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data

          // 检查业务状态
          if (data.success) {
            resolve(data)
          } else {
            // 业务失败
            uni.showToast({
              title: data.message || '请求失败',
              icon: 'none',
              duration: 2000
            })
            reject(data)
          }
        } else {
          // HTTP 错误
          uni.showToast({
            title: `请求失败: ${res.statusCode}`,
            icon: 'none',
            duration: 2000
          })
          reject(res)
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        uni.showToast({
          title: '网络请求失败',
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
