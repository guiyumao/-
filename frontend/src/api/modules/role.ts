import type { MenuTreeItem } from '../../types/auth'
import { getResult, postResult, putResult } from '../http'

export interface RoleItem {
    id: number
    roleCode: string
    roleName: string
    description?: string
    status: number
}

export interface RoleSavePayload {
    roleName: string
    roleCode: string
}

export function fetchRoles() {
    return getResult<RoleItem[]>('/roles')
}

export function createRole(data: RoleSavePayload) {
    return postResult<RoleItem>('/roles', data)
}

export function updateRole(id: number, data: RoleSavePayload) {
    return putResult<RoleItem>(`/roles/${id}`, data)
}

export function fetchRoleMenuIds(roleId: number) {
    return getResult<number[]>(`/roles/${roleId}/menu-ids`)
}

export function assignRoleMenus(roleId: number, menuIds: number[]) {
    return postResult<boolean>(`/roles/${roleId}/menus`, { menuIds })
}

export function fetchMenuTree() {
    return getResult<MenuTreeItem[]>('/menus/tree')
}
