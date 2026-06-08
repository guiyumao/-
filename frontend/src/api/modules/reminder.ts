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

export interface UserNotification {
    id: number
    receiverUserId: number
    senderUserId?: number
    title: string
    content: string
    notificationType: string
    relatedType?: string
    relatedId?: number
    readStatus: number
    createTime?: string
}

export function getReminders() {
    return getResult<ReminderResponse>('/reminders')
}

export function getMyNotifications() {
    return getResult<UserNotification[]>('/notifications/mine')
}
