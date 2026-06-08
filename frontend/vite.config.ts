import path from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
    const projectRoot = path.resolve(__dirname, '..')
    const env = loadEnv(mode, projectRoot, '')

    return {
        envDir: projectRoot,
        plugins: [vue()],
        server: {
            host: env.VITE_FRONTEND_HOST || '0.0.0.0',
            port: Number(env.VITE_FRONTEND_PORT || 5173),
        },
    }
})
