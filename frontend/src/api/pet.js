import request from '@/utils/request'

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
    uni.uploadFile({
      url: request.defaults.baseURL + '/api/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': uni.getStorageSync('token')
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.success) {
          resolve(data)
        } else {
          reject(new Error(data.message || '上传失败'))
        }
      },
      fail: (err) => {
        reject(new Error('上传失败'))
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
