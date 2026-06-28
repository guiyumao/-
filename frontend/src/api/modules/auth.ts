import type { AuthContextResponse, AvailableRole, LoginResponse, UserProfile } from '../../types/auth'
import { getResult, postResult } from '../http'

export interface RegisterRequest {
    laboratoryId: number
    username: string
    password: string
    realName: string
    userNo: string
    phone?: string
    email?: string
    registerType: 'teacher' | 'student'
}

export interface RegisterResponse {
    userId: number
    username: string
    realName: string
    roleCode: string
    message: string
}

export interface PublicLaboratoryOption {
    id: number
    laboratoryCode: string
    laboratoryName: string
    location?: string
}

export function login(username: string, password: string, roleCode: string) {
    return postResult<LoginResponse>('/auth/login', { username, password, roleCode })
}

export function register(data: RegisterRequest) {
    return postResult<RegisterResponse>('/auth/register', data)
}

export function getCurrentUser() {
    return getResult<UserProfile>('/auth/me')
}

export function getAuthContext() {
    return getResult<AuthContextResponse>('/auth/context')
}

export function getPublicLaboratories() {
    return getResult<PublicLaboratoryOption[]>('/laboratories/public-options')
}

export function getAvailableRoles(username: string) {
    return getResult<AvailableRole[]>('/auth/available-roles', { username })
}
