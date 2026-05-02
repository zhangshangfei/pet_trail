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
  return 'http://124.222.51.71:8080'
  // #endif

  // #ifdef H5
  if (process.env.NODE_ENV === 'production') {
    return 'https://aipetfamily.cn'
  }
  return 'http://124.222.51.71:8080'
  // #endif

  return 'http://124.222.51.71:8080'
}

// 云托管配置
const getCloudConfig = () => {
  return {
    // 云托管环境 ID
    env: 'prod-3gyc3tpg28270da6',
    // 云托管服务名称
    service: 'springboot-4fyd'
  }
}

/**
 * 文件上传使用的 HTTP 根地址。
 * 微信小程序走云托管时，业务接口用 callContainer，但 uni.uploadFile 必须是完整 HTTPS URL，
 * 因此需要单独配置公网可访问的上传入口（默认同 H5 生产域名）。
 */
const getUploadHttpBase = () => {
  const api = getApiBaseUrl()
  return api.replace(/\/$/, '')
}

export default {
  // API 基础地址（'cloud' 表示使用云托管）
  VITE_API_BASE_URL: getApiBaseUrl(),

  /** 上传文件时使用的 HTTP 根地址（无尾部斜杠） */
  VITE_UPLOAD_HTTP_BASE: getUploadHttpBase(),

  // 云托管配置
  VITE_CLOUD_CONFIG: getCloudConfig(),

  // 应用标题
  VITE_APP_TITLE: '宠迹 - 宠物生活伴侣',

  // Token 过期时间（7 天）
  TOKEN_EXPIRE_DAYS: 7,

  // 上传文件大小限制（10MB）
  UPLOAD_MAX_SIZE: 10 * 1024 * 1024,

  // 请求超时时间（30 秒）
  REQUEST_TIMEOUT: 30000
}
