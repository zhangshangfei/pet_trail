import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useAdminStore } from '../store/admin'

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
  const adminStore = useAdminStore()
  if (adminStore.token) {
    config.headers.Authorization = `Bearer ${adminStore.token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    if (response.config.responseType === 'blob') {
      if (response.data && response.data.type && response.data.type.includes('application/json')) {
        return response.data.text().then(text => {
          const err = JSON.parse(text)
          ElMessage.error(err.message || '请求失败')
          if (err.code === 401) {
            const adminStore = useAdminStore()
            adminStore.logout()
            router.push('/login')
          }
          return Promise.reject(new Error(err.message))
        })
      }
      return response
    }
    const res = response.data
    if (res.success === false) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const adminStore = useAdminStore()
        adminStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        const adminStore = useAdminStore()
        adminStore.logout()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (error.response.status === 403) {
        ElMessage.error(error.response.data?.message || '权限不足')
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
