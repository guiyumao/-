import { postResult, putResult } from '../http'

export interface CreateUserRequest {
    laboratoryId: number
    username: string
    initialPassword: string
    realName: string
    userNo: string
    phone: string
    email: string
    userType: number
    status: number
}

export interface UpdateUserRequest {
    laboratoryId: number
    username: string
    realName: string
    userNo: string
    phone: string
    email: string
    userType: number
    status: number
}

export function createUser(data: CreateUserRequest) {
    return postResult<boolean>('/users', data)
}

export function updateUser(id: number, data: UpdateUserRequest) {
    return putResult<boolean>(`/users/${id}`, data)
}

export function resetUserPassword(id: number, newPassword: string) {
    return putResult<boolean>(`/users/${id}/reset-password`, { newPassword })
}
