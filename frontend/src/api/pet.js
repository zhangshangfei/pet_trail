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

function readFileAsBase64(filePath) {
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success: (res) => resolve(res.data),
      fail: (err) => reject(new Error(err.errMsg || '读取文件失败'))
    })
  })
}

function getFileName(filePath) {
  if (!filePath) return 'upload.jpg'
  const parts = filePath.replace(/\\/g, '/').split('/')
  const name = parts[parts.length - 1] || 'upload.jpg'
  return name
}

function guessContentType(fileName) {
  const ext = (fileName.split('.').pop() || '').toLowerCase()
  const map = {
    jpg: 'image/jpeg', jpeg: 'image/jpeg', png: 'image/png',
    gif: 'image/gif', webp: 'image/webp', bmp: 'image/bmp',
    mp4: 'video/mp4', mov: 'video/quicktime', avi: 'video/x-msvideo'
  }
  return map[ext] || 'application/octet-stream'
}

function getFileInfo(filePath) {
  return new Promise((resolve) => {
    uni.getFileInfo({
      filePath,
      success: (res) => resolve(res.size),
      fail: () => resolve(0)
    })
  })
}

async function ensureCompressed(filePath, maxSizeKB = 2048) {
  const size = await getFileInfo(filePath)
  if (size <= maxSizeKB * 1024) return filePath
  let quality = 50
  if (size > maxSizeKB * 1024 * 2) quality = 30
  try {
    const compressed = await compressImage(filePath, quality)
    return compressed
  } catch (e) {
    return filePath
  }
}

function doUploadHttp(filePath) {
  return new Promise((resolve, reject) => {
    const uploadUrl = resolveUploadUrl()
    const token = uni.getStorageSync('token') || ''
    const header = {}
    if (token) {
      header.Authorization = `Bearer ${token}`
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

async function doUploadBase64(filePath) {
  const fileName = getFileName(filePath)
  const contentType = guessContentType(fileName)
  let uploadPath = filePath
  if (contentType.startsWith('image/')) {
    uploadPath = await ensureCompressed(filePath, 2048)
  }
  const fileSize = await getFileInfo(uploadPath)
  if (fileSize > 2 * 1024 * 1024) {
    throw new Error('文件过大，请选择更小的图片')
  }
  const base64Data = await readFileAsBase64(uploadPath)
  const result = await request.post('/api/upload/base64', {
    fileBase64: base64Data,
    fileName,
    contentType
  })
  return result
}

async function doUpload(filePath) {
  try {
    return await doUploadHttp(filePath)
  } catch (httpError) {
    console.warn('HTTP上传失败，尝试base64方式:', httpError.message)
    if (BASE_URL === 'cloud') {
      try {
        return await doUploadBase64(filePath)
      } catch (base64Error) {
        console.error('base64上传也失败:', base64Error.message)
        throw base64Error
      }
    }
    throw httpError
  }
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
