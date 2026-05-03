import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useAdminStore } from '../store/admin'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const request = axios.create({
  baseURL,
  timeout: 30000
})

const pendingRequests = new Map()
const responseCache = new Map()
const CACHE_MAX_SIZE = 100
const CACHE_TTL = 3000
const RETRY_COUNT = 1
const RETRY_DELAY = 1000

function generateRequestKey(config) {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

function addPendingRequest(config) {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    config.__retryCount = config.__retryCount || 0
    return
  }
  config.cancelToken = config.cancelToken || new axios.CancelToken(cancel => {
    pendingRequests.set(key, cancel)
  })
}

function removePendingRequest(config) {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    const cancel = pendingRequests.get(key)
    cancel(key)
    pendingRequests.delete(key)
  }
}

function getCacheKey(config) {
  if (config.method !== 'get') return null
  const { url, params } = config
  return `GET:${url}:${JSON.stringify(params)}`
}

function getCache(config) {
  const key = getCacheKey(config)
  if (!key) return null
  const cached = responseCache.get(key)
  if (!cached) return null
  if (Date.now() - cached.timestamp > (config.cacheTTL || CACHE_TTL)) {
    responseCache.delete(key)
    return null
  }
  return cached.data
}

function setCache(config, data) {
  const key = getCacheKey(config)
  if (!key) return
  if (responseCache.size >= CACHE_MAX_SIZE) {
    const oldestKey = responseCache.keys().next().value
    responseCache.delete(oldestKey)
  }
  responseCache.set(key, { data, timestamp: Date.now() })
}

export function cancelAllRequests() {
  pendingRequests.forEach((cancel, key) => {
    cancel(key)
  })
  pendingRequests.clear()
}

export function clearCache() {
  responseCache.clear()
}

request.interceptors.request.use(config => {
  const adminStore = useAdminStore()
  if (adminStore.token) {
    config.headers.Authorization = `Bearer ${adminStore.token}`
  }

  if (config.method === 'get' && config.cache !== false) {
    const cached = getCache(config)
    if (cached) {
      config.__fromCache = true
      return Promise.reject({ __cached: true, data: cached, config })
    }
  }

  removePendingRequest(config)
  addPendingRequest(config)

  return config
})

request.interceptors.response.use(
  response => {
    removePendingRequest(response.config)

    if (response.config.method === 'get' && response.config.cache !== false) {
      setCache(response.config, response)
    }

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
    if (error.__cached) {
      return Promise.resolve(error.data)
    }

    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }

    if (error.config) {
      removePendingRequest(error.config)

      const retryCount = error.config.__retryCount || 0
      const maxRetry = error.config.retryCount ?? RETRY_COUNT
      const shouldRetry = !error.response && retryCount < maxRetry

      if (shouldRetry) {
        error.config.__retryCount = retryCount + 1
        const delay = RETRY_DELAY * error.config.__retryCount
        return new Promise(resolve => {
          setTimeout(() => resolve(request(error.config)), delay)
        })
      }
    }

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
