export interface MenuItem {
    id: number
    parentId?: number | null
    path: string
    label: string
    permission?: string
    component?: string
    sortOrder?: number
    icon?: string
}

export interface MenuTreeItem {
    id: number
    parentId?: number | null
    menuCode: string
    menuName: string
    routePath: string
    permissionCode?: string
    menuType: number
    sortOrder?: number
    visible: number
    status: number
    children: MenuTreeItem[]
}

export interface UserProfile {
    id: number
    laboratoryId: number
    username: string
    realName: string
    userType: number
    roleCodes: string[]
}

export interface LoginResponse {
    token: string
    expireMinutes: number
    user: UserProfile
    menus: MenuItem[]
    permissions: string[]
}

export interface AuthContextResponse {
    user: UserProfile
    menus: MenuItem[]
    permissions: string[]
}
