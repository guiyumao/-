import { getResult } from '../http'

export interface InventoryAlertRow {
    consumableId: number
    consumableCode: string
    consumableName: string
    currentStock: number
    safetyStock: number
    storageLocation: string
}

export function getInventoryAlerts(params: Record<string, unknown>) {
    return getResult<{
        current: number
        pageSize: number
        total: number
        pages: number
        records: InventoryAlertRow[]
    }>('/inventory/alert', params)
}
