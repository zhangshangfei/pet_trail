/**
 * 图片压缩工具
 * 用于微信小程序图片上传前的压缩处理
 */

/**
 * 压缩单张图片
 * @param {Object} options - 压缩选项
 * @param {string} options.filePath - 图片文件路径
 * @param {number} [options.quality=80] - 压缩质量 (0-100)
 * @returns {Promise<string>} - 压缩后的临时文件路径
 */
export const compressImage = (options) => {
  const {
    filePath,
    quality = 80
  } = options

  return new Promise((resolve) => {
    uni.compressImage({
      src: filePath,
      quality,
      success: (res) => {
        resolve(res.tempFilePath)
      },
      fail: (err) => {
        console.warn('图片压缩失败，使用原图:', err)
        resolve(filePath)
      }
    })
  })
}

/**
 * 批量压缩图片
 * @param {Array<string>} filePaths - 图片文件路径数组
 * @param {Object} options - 压缩选项
 * @returns {Promise<Array<string>>} - 压缩后的文件路径数组
 */
export const compressImages = async (filePaths, options = {}) => {
  const compressedPaths = []

  for (let i = 0; i < filePaths.length; i++) {
    try {
      const compressedPath = await compressImage({
        filePath: filePaths[i],
        ...options
      })
      compressedPaths.push(compressedPath)
    } catch (error) {
      console.error(`压缩第 ${i + 1} 张图片失败:`, error)
      compressedPaths.push(filePaths[i])
    }
  }

  return compressedPaths
}

/**
 * 估算图片文件大小
 * @param {string} filePath - 图片文件路径
 * @returns {Promise<number>} - 文件大小（字节）
 */
export const getImageSize = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.getFileInfo({
      filePath,
      success: (res) => {
        resolve(res.size)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} - 格式化后的大小
 */
export const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}
