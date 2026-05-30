import type { AuthContextResponse, LoginResponse, UserProfile } from '../../types/auth'
import { getResult, postResult } from '../http'

export function login(username: string, password: string) {
    return postResult<LoginResponse>('/auth/login', { username, password })
}

export function getCurrentUser() {
    return getResult<UserProfile>('/auth/me')
}

export function getAuthContext() {
    return getResult<AuthContextResponse>('/auth/context')
}
