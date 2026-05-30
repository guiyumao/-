import { defineStore } from 'pinia'
import { getAuthContext, login as loginApi } from '../api/modules/auth'
import type { MenuItem, UserProfile } from '../types/auth'
import { clearToken, getToken, setToken } from '../utils/token'

interface AuthState {
    token: string
    user: UserProfile | null
    menus: MenuItem[]
    permissions: string[]
    initialized: boolean
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        token: getToken() ?? '',
        user: null,
        menus: [],
        permissions: [],
        initialized: false,
    }),
    getters: {
        isAuthenticated: (state) => Boolean(state.token),
        sidebarMenus: (state) => {
            const visibleMenus = [...state.menus].sort((a, b) => (a.sortOrder ?? 9999) - (b.sortOrder ?? 9999))
            const itemMap = new Map<number, MenuItem & { children: MenuItem[] }>()
            const roots: Array<MenuItem & { children: MenuItem[] }> = []

            visibleMenus.forEach((menu) => {
                itemMap.set(menu.id, { ...menu, children: [] })
            })

            visibleMenus.forEach((menu) => {
                const current = itemMap.get(menu.id)
                if (!current) {
                    return
                }
                if (menu.parentId && itemMap.has(menu.parentId)) {
                    itemMap.get(menu.parentId)?.children.push(current)
                } else {
                    roots.push(current)
                }
            })

            roots.forEach((root) => {
                root.children.sort((a, b) => (a.sortOrder ?? 9999) - (b.sortOrder ?? 9999))
            })

            return roots
        },
        permissionSet: (state) => new Set(state.permissions),
    },
    actions: {
        async login(username: string, password: string) {
            const result = await loginApi(username, password)
            this.token = result.data.token
            this.user = result.data.user
            this.menus = result.data.menus
            this.permissions = result.data.permissions
            this.initialized = true
            setToken(result.data.token)
            return result.data
        },
        async bootstrap() {
            if (!this.token) {
                this.initialized = true
                return
            }
            try {
                const result = await getAuthContext()
                this.user = result.data.user
                this.menus = result.data.menus
                this.permissions = result.data.permissions
            } catch (error) {
                this.logout()
                throw error
            } finally {
                this.initialized = true
            }
        },
        logout() {
            this.token = ''
            this.user = null
            this.menus = []
            this.permissions = []
            this.initialized = true
            clearToken()
        },
        hasPermission(permission: string) {
            return this.permissions.includes(permission)
        },
    },
})
