import { getResult } from '../http'

export interface AuditLogRow {
    id: number
    userId?: number
    username?: string
    operation: string
    method: string
    params?: string
    ip?: string
    createTime: string
}

export function getAuditLogs(params: Record<string, unknown>) {
    return getResult<{
        current: number
        pageSize: number
        total: number
        pages: number
        records: AuditLogRow[]
    }>('/audit-logs', params)
}
