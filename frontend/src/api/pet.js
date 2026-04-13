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
 * 上传图片（multipart/form-data，字段名 file，对应后端 POST /api/upload）
 * @param {string} filePath - 图片本地路径
 */
export const uploadImage = (filePath) => {
  return new Promise((resolve, reject) => {
    const uploadUrl = resolveUploadUrl()
    const token = uni.getStorageSync('token') || ''
    const header = {}
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
    
    // 微信小程序云托管需要添加 X-WX-SERVICE header
    if (BASE_URL === 'cloud') {
      header['X-WX-SERVICE'] = config.VITE_CLOUD_CONFIG.service
    }

    console.log('[uploadImage] 上传地址:', uploadUrl)
    console.log('[uploadImage] 请求头:', header)

    uni.uploadFile({
      url: uploadUrl,
      filePath,
      name: 'file',
      header,
      success: (res) => {
        console.log('[uploadImage] 上传响应:', res)
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error(`上传失败：HTTP ${res.statusCode}`))
          return
        }
        let data = res.data
        console.log('[uploadImage] 解析前数据:', res.data)
        if (typeof data === 'string') {
          try {
            data = JSON.parse(data)
            console.log('[uploadImage] 解析后数据:', data)
          } catch (e) {
            console.error('[uploadImage] JSON 解析失败:', e)
            reject(new Error('上传响应解析失败'))
            return
          }
        }
        if (data && data.success) {
          console.log('[uploadImage] 上传成功, URL:', data.data?.url)
          resolve(data)
        } else {
          reject(new Error((data && data.message) || '上传失败'))
        }
      },
      fail: (err) => {
        console.error('[uploadImage] 上传失败:', err)
        reject(new Error((err && err.errMsg) || '上传失败'))
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
