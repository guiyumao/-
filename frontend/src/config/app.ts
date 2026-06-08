const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() || '/api'
const authHeaderPrefix = import.meta.env.VITE_AUTH_HEADER_PREFIX?.trim() || 'Bearer'

export const appConfig = {
    apiBaseUrl,
    authHeaderPrefix,
}

export function buildApiUrl(path: string) {
    const normalizedBase = apiBaseUrl.endsWith('/') ? apiBaseUrl.slice(0, -1) : apiBaseUrl
    const normalizedPath = path.startsWith('/') ? path : `/${path}`
    return `${normalizedBase}${normalizedPath}`
}

export function withAuthHeader(token?: string | null) {
    return token ? ({ Authorization: `${authHeaderPrefix} ${token}` } satisfies Record<string, string>) : undefined
}
