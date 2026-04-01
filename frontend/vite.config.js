import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  return {
    plugins: [uni()],
    server: {
      port: 8080,
      host: '0.0.0.0'
    },
    define: {
      // 定义环境变量给应用使用
      'process.env': {
        NODE_ENV: mode,
        VITE_API_BASE_URL: env.VITE_API_BASE_URL || 'http://localhost:8080'
      }
    }
  }
})
