import { getResult } from '../http'

export interface ReminderItem {
    reminderType: string
    itemName: string
    laboratoryName: string
    batchNo?: string
    dueDate: string
    remainingQuantity?: number
}

export interface ReminderResponse {
    pendingCalibrations: ReminderItem[]
    expiringConsumables: ReminderItem[]
}

export function getReminders() {
    return getResult<ReminderResponse>('/reminders')
}
