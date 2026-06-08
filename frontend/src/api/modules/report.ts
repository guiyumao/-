import { buildApiUrl, withAuthHeader } from '../../config/app'
import { getToken } from '../../utils/token'
import { getResult } from '../http'

export interface ReportRow {
    reportType: string
    itemCode: string
    itemName: string
    laboratoryName: string
    laboratoryId?: number
    businessDate: string
    quantity: string
    amount: string
    statusText: string
}

export interface ReportQuery {
    current?: number
    pageSize?: number
    labId?: number
    startDate?: string
    endDate?: string
    type?: string
}

export function getReportSummary(params: ReportQuery) {
    return getResult<{
        current: number
        pageSize: number
        total: number
        pages: number
        records: ReportRow[]
    }>('/reports/summary', params as unknown as Record<string, unknown>)
}

function buildReportExportUrl(params: ReportQuery) {
    const search = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            search.append(key, String(value))
        }
    })
    return `${buildApiUrl('/reports/summary/export')}?${search.toString()}`
}

export async function downloadReportSummary(params: ReportQuery) {
    const token = getToken()
    const response = await fetch(buildReportExportUrl(params), {
        headers: withAuthHeader(token),
    })
    if (!response.ok) {
        throw new Error('瀵煎嚭澶辫触')
    }
    return response.blob()
}
