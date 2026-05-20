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
    heic: 'image/heic', heif: 'image/heif',
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

/**
 * 根据文件大小动态计算压缩质量
 * 目标：压缩后文件不超过 targetSizeKB
 * nginx 默认 client_max_body_size 通常为 1MB
 * uni.uploadFile 走 multipart，额外开销小
 * base64 编码增加约 33%，需更激进压缩
 */
function calcQuality(fileSize, targetSizeKB) {
  const targetBytes = targetSizeKB * 1024
  if (fileSize <= targetBytes) return 80
  const ratio = targetBytes / fileSize
  // 质量与文件大小大致成正比，但需要留余量
  let quality = Math.floor(ratio * 90)
  return Math.max(10, Math.min(80, quality))
}

/**
 * 多轮压缩确保文件不超过目标大小
 */
async function compressToSize(filePath, targetSizeKB) {
  let currentPath = filePath
  let currentSize = await getFileInfo(currentPath)

  if (currentSize <= targetSizeKB * 1024) return currentPath

  // 第一轮：根据大小计算质量
  let quality = calcQuality(currentSize, targetSizeKB)
  currentPath = await compressImage(currentPath, quality)
  currentSize = await getFileInfo(currentPath)

  if (currentSize <= targetSizeKB * 1024) return currentPath

  // 第二轮：更激进压缩
  quality = Math.max(10, Math.floor(quality * 0.5))
  currentPath = await compressImage(currentPath, quality)
  currentSize = await getFileInfo(currentPath)

  if (currentSize <= targetSizeKB * 1024) return currentPath

  // 第三轮：最低质量
  currentPath = await compressImage(currentPath, 10)
  return currentPath
}

async function doUploadHttp(filePath) {
  let uploadFilePath = filePath
  const fileName = getFileName(filePath)
  const ext = (fileName.split('.').pop() || '').toLowerCase()
  // 非视频文件先压缩（确保格式正确且体积合理）
  if (!['mp4', 'mov', 'avi'].includes(ext)) {
    try {
      uploadFilePath = await compressToSize(filePath, 5120)
    } catch (e) { /* ignore, use original */ }
  }

  return new Promise((resolve, reject) => {
    const uploadUrl = resolveUploadUrl()
    const token = uni.getStorageSync('token') || ''
    const header = {}
    if (token) {
      header.Authorization = `Bearer ${token}`
    }

    uni.uploadFile({
      url: uploadUrl,
      filePath: uploadFilePath,
      name: 'file',
      header,
      timeout: 180000,
      success: (res) => {
        if (res.statusCode === 413) {
          reject(new Error('图片过大，请选择更小的图片'))
          return
        }
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
  const isVideo = contentType.startsWith('video/')
  if (isVideo) {
    throw new Error('视频文件过大，请选择更短的视频（建议30秒以内）')
  }
  // base64 编码增加约 33%，目标 3MB 原图 → 编码后约 4MB
  const uploadPath = await compressToSize(filePath, 3072)
  const fileSize = await getFileInfo(uploadPath)
  if (fileSize > 10 * 1024 * 1024) {
    throw new Error('图片文件过大，请选择更小的图片')
  }
  const base64Data = await readFileAsBase64(uploadPath)
  const result = await request.post('/api/upload/base64', {
    fileBase64: base64Data,
    fileName,
    contentType
  }, {}, 60000)
  return result
}

async function doUpload(filePath) {
  try {
    return await doUploadHttp(filePath)
  } catch (httpError) {
    // 413 不再重试 base64（同样会 413）
    if (httpError.message && httpError.message.includes('图片过大')) {
      throw httpError
    }
    console.warn('HTTP上传失败，尝试base64方式:', httpError.message)
    try {
      return await doUploadBase64(filePath)
    } catch (base64Error) {
      console.error('base64上传也失败:', base64Error.message)
      throw base64Error
    }
  }
}

export const uploadImage = (filePath) => {
  return doUpload(filePath)
}

export const uploadFile = (filePath) => {
  return doUpload(filePath)
}
