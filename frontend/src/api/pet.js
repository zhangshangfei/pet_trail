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

/**
 * 获取宠物列表
 */
export const getPetList = () => {
  return request.get('/api/pets')
}

/**
 * 上传图片
 * @param {string} filePath - 图片本地路径
 */
export const uploadImage = (filePath) => {
  return new Promise((resolve, reject) => {
    // 读取文件为 base64
    const fs = wx.getFileSystemManager()
    fs.readFile({
      filePath: filePath,
      encoding: 'base64',
      success: (readRes) => {
        // 获取文件扩展名
        const ext = filePath.substring(filePath.lastIndexOf('.') + 1)
        const fileName = `upload_${Date.now()}.${ext}`

        // 通过云托管上传
        wx.cloud.callContainer({
          config: {
            env: config.VITE_CLOUD_CONFIG.env
          },
          path: '/api/upload/base64',
          method: 'POST',
          header: {
            'X-WX-SERVICE': config.VITE_CLOUD_CONFIG.service,
            'Content-Type': 'application/json'
          },
          data: {
            fileName: fileName,
            fileBase64: readRes.data,
            contentType: 'image/' + ext
          },
          success: (res) => {
            if (res.statusCode >= 200 && res.statusCode < 300) {
              const data = res.data
              if (data && data.success) {
                resolve(data)
              } else {
                reject(new Error((data && data.message) || '上传失败'))
              }
            } else {
              reject(new Error(`上传失败：HTTP ${res.statusCode}`))
            }
          },
          fail: (err) => {
            reject(new Error((err && err.errMsg) || '上传失败'))
          }
        })
      },
      fail: (err) => {
        reject(new Error('读取文件失败'))
      }
    })
  })
}

/**
 * 创建宠物
 * @param {object} data - 宠物信息
 */
export const createPet = (data) => {
  return request.post('/api/pets', null, {
    params: data
  })
}

/**
 * 获取宠物详情
 * @param {number} petId - 宠物 ID
 */
export const getPetDetail = (petId) => {
  return request.get(`/api/pets/${petId}`)
}

/**
 * 更新宠物信息
 * @param {number} petId - 宠物 ID
 * @param {object} data - 更新的宠物信息
 */
export const updatePet = (petId, data) => {
  return request.put(`/api/pets/${petId}`, null, {
    params: data
  })
}

/**
 * 删除宠物
 * @param {number} petId - 宠物 ID
 */
export const deletePet = (petId) => {
  return request.delete(`/api/pets/${petId}`)
}

/**
 * 更新宠物体重
 * @param {number} petId - 宠物 ID
 * @param {number} weight - 体重
 */
export const updatePetWeight = (petId, weight) => {
  return request.put(`/api/pets/${petId}/weight`, null, {
    params: { weight }
  })
}
