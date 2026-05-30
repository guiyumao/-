import type { PageResult } from '../types/crud'
import { deleteResult, getResult, postResult, putResult } from './http'

export async function fetchPage<T>(endpoint: string, params: Record<string, unknown>) {
    return getResult<PageResult<T>>(endpoint, params)
}

export async function fetchOne<T>(endpoint: string, id: number) {
    return getResult<T>(`${endpoint}/${id}`)
}

export async function createOne<T>(endpoint: string, data: T) {
    return postResult<boolean>(endpoint, data)
}

export async function updateOne<T>(endpoint: string, id: number, data: T) {
    return putResult<boolean>(`${endpoint}/${id}`, data)
}

export async function deleteOne(endpoint: string, id: number) {
    return deleteResult<boolean>(`${endpoint}/${id}`)
}
