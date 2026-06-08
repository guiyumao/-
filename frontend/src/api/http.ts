import axios from 'axios'
import { appConfig } from '../config/app'
import type { ApiResult } from '../types/crud'
import { clearToken, getToken } from '../utils/token'

const http = axios.create({
    baseURL: appConfig.apiBaseUrl,
    timeout: 10000,
})

http.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
        config.headers.Authorization = `${appConfig.authHeaderPrefix} ${token}`
    }
    return config
})

http.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error?.response?.status === 401) {
            clearToken()
            window.location.href = '/login'
        }
        return Promise.reject(error)
    },
)

export async function getResult<T>(url: string, params?: Record<string, unknown>) {
    const response = await http.get<ApiResult<T>>(url, { params })
    return response.data
}

export async function postResult<T>(url: string, data: unknown) {
    const response = await http.post<ApiResult<T>>(url, data)
    return response.data
}

export async function putResult<T>(url: string, data: unknown) {
    const response = await http.put<ApiResult<T>>(url, data)
    return response.data
}

export async function postRaw(url: string, data: unknown) {
    return http.post(url, data)
}

export async function deleteResult<T>(url: string) {
    const response = await http.delete<ApiResult<T>>(url)
    return response.data
}
