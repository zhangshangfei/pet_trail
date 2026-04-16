import request from '@/utils/request'
import config from '@/config/env'

const BASE_URL = config.VITE_API_BASE_URL
const UPLOAD_HTTP_BASE = (config.VITE_UPLOAD_HTTP_BASE || '').replace(/\/$/, '')

function resolveUploadUrl() {
  if (BASE_URL === 'cloud') {
    const base = UPLOAD_HTTP_BASE || 'https://api.pettrail.com'
    return `${base}/api/upload`
  }
  return `${String(BASE_URL).replace(/\/$/, '')}/api/upload`
}

export const getPetList = () => {
  return request.get('/api/pets')
}

function doUpload(filePath) {
  return new Promise((resolve, reject) => {
    const uploadUrl = resolveUploadUrl()
    const token = uni.getStorageSync('token') || ''
    const header = {}
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
    if (BASE_URL === 'cloud') {
      header['X-WX-SERVICE'] = config.VITE_CLOUD_CONFIG.service
    }

    uni.uploadFile({
      url: uploadUrl,
      filePath,
      name: 'file',
      header,
      success: (res) => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error(`上传失败：HTTP ${res.statusCode}`))
          return
        }
        let data = res.data
        if (typeof data === 'string') {
          try {
            data = JSON.parse(data)
          } catch (e) {
            reject(new Error('上传响应解析失败'))
            return
          }
        }
        if (data && data.success) {
          resolve(data)
        } else {
          reject(new Error((data && data.message) || '上传失败'))
        }
      },
      fail: (err) => {
        reject(new Error((err && err.errMsg) || '上传失败'))
      }
    })
  })
}

export const uploadImage = (filePath) => {
  return doUpload(filePath)
}

export const uploadFile = (filePath) => {
  return doUpload(filePath)
}

export const compressImage = (filePath, quality = 80) => {
  return new Promise((resolve) => {
    uni.compressImage({
      src: filePath,
      quality,
      success: (res) => {
        resolve(res.tempFilePath)
      },
      fail: () => {
        resolve(filePath)
      }
    })
  })
}

export const createPet = (data) => {
  return request.post('/api/pets', data)
}

export const getPetDetail = (petId) => {
  return request.get(`/api/pets/${petId}`)
}

export const updatePet = (petId, data) => {
  return request.put(`/api/pets/${petId}`, data)
}

export const deletePet = (petId) => {
  return request.delete(`/api/pets/${petId}`)
}

export const updatePetWeight = (petId, weight) => {
  return request.put(`/api/pets/${petId}/weight`, { weight })
}
