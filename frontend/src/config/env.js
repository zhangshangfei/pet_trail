/**
 * 环境配置
 *
 * uni-app 中环境变量说明：
 * - process.env.NODE_ENV: 'development' | 'production'
 * - 条件编译：#ifdef H5, #ifdef MP-WEIXIN 等
 */

// 获取 API 基础地址
const getApiBaseUrl = () => {
  // #ifdef MP-WEIXIN
  // 微信小程序环境 - 开发环境使用本地地址
  return 'http://localhost:8080'
  // #endif

  // #ifdef H5
  // H5 环境
  if (process.env.NODE_ENV === 'production') {
    return 'https://api.pettrail.com'
  }
  // 开发环境
  return 'http://localhost:8080'
  // #endif

  // 默认返回
  return 'http://localhost:8080'
}

export default {
  // API 基础地址
  VITE_API_BASE_URL: getApiBaseUrl(),

  // 应用标题
  VITE_APP_TITLE: '宠迹 - 宠物生活伴侣',

  // Token 过期时间（7 天）
  TOKEN_EXPIRE_DAYS: 7,

  // 上传文件大小限制（10MB）
  UPLOAD_MAX_SIZE: 10 * 1024 * 1024,

  // 请求超时时间（30 秒）
  REQUEST_TIMEOUT: 30000
}
