import { getResult } from '../http'

export interface DashboardStatsResponse {
    laboratoryCount: number
    equipmentCount: number
    consumableCount: number
    hazardousCount: number
    currentBorrowCount: number
    monthlyConsumableAmount: number
    pendingCalibrations: number
    expiringConsumables: number
    consumableTrend: Array<{ label: string; value: number }>
}

export function getDashboardStats() {
    return getResult<DashboardStatsResponse>('/dashboard/stats')
}
