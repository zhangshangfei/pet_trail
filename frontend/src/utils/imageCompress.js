/**
 * 图片压缩工具
 * 用于微信小程序图片上传前的压缩处理
 */

/**
 * 压缩图片
 * @param {Object} options - 压缩选项
 * @param {string} options.filePath - 图片文件路径
 * @param {number} [options.quality=80] - 压缩质量 (0-100)
 * @param {number} [options.maxWidth=1920] - 最大宽度
 * @param {number} [options.maxHeight=1920] - 最大高度
 * @returns {Promise<string>} - 压缩后的临时文件路径
 */
export const compressImage = (options) => {
  const {
    filePath,
    quality = 80,
    maxWidth = 1920,
    maxHeight = 1920
  } = options

  return new Promise((resolve, reject) => {
    // 获取图片信息
    uni.getImageInfo({
      src: filePath,
      success: (imageInfo) => {
        // 计算压缩后的尺寸
        let { width, height } = imageInfo
        
        // 如果图片尺寸超过限制，等比缩放
        if (width > maxWidth || height > maxHeight) {
          const ratio = Math.min(maxWidth / width, maxHeight / height)
          width = Math.floor(width * ratio)
          height = Math.floor(height * ratio)
        }

        // 使用 canvas 压缩
        const ctx = uni.createCanvasContext('compressCanvas')
        
        // 设置 canvas 尺寸
        ctx.canvas = {
          width: width,
          height: height
        }

        // 绘制图片
        ctx.drawImage(filePath, 0, 0, width, height)
        
        // 导出压缩后的图片
        ctx.draw(false, () => {
          setTimeout(() => {
            uni.canvasToTempFilePath({
              canvasId: 'compressCanvas',
              destWidth: width,
              destHeight: height,
              fileType: 'jpg',
              quality: quality / 100,
              success: (res) => {
                resolve(res.tempFilePath)
              },
              fail: (err) => {
                console.error('图片压缩失败:', err)
                // 如果压缩失败，返回原图
                resolve(filePath)
              }
            })
          }, 100)
        })
      },
      fail: (err) => {
        console.error('获取图片信息失败:', err)
        reject(new Error('获取图片信息失败'))
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
      // 压缩失败保留原图
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
