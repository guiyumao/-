/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_API_BASE_URL?: string
    readonly VITE_AUTH_HEADER_PREFIX?: string
    readonly VITE_FRONTEND_HOST?: string
    readonly VITE_FRONTEND_PORT?: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<Record<string, never>, Record<string, never>, any>
    export default component
}
