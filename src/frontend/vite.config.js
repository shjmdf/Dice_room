import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendPort = env.DICE_ROOM_SERVER_PORT || '8080'

  return {
    plugins: [vue()],
    server: {
      host: env.DICE_ROOM_FRONTEND_HOST || '0.0.0.0',
      port: Number(env.DICE_ROOM_FRONTEND_PORT || 5173),
      proxy: {
        '/api': {
          target: env.VITE_API_TARGET || `http://localhost:${backendPort}`,
          changeOrigin: true
        }
      }
    }
  }
})
