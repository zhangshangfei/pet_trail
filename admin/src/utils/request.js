import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const getBaseURL = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  return ''
}

const request = axios.create({
  baseURL: getBaseURL(),
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success === false) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
        router.push('/login')
      }
      if (res.code === 403) {
        ElMessage.error('权限不足')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (error.response.status === 403) {
        ElMessage.error('权限不足')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
