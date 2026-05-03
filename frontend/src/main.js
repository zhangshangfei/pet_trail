import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import config from './config/env'
import request from '@/utils/request'

const BASE_URL = config.VITE_API_BASE_URL

// #ifdef MP-WEIXIN
if (BASE_URL === 'cloud' && wx.cloud) {
  wx.cloud.init({
    env: config.VITE_CLOUD_CONFIG.env,
    traceUser: true
  })
  console.log('微信云开发初始化完成，环境ID:', config.VITE_CLOUD_CONFIG.env)
} else {
  console.log('使用本地服务模式，API地址:', BASE_URL)
}
// #endif

uni.$request = request

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()

  app.use(pinia)

  return {
    app,
    pinia
  }
}
