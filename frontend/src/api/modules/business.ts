import { createOne, fetchPage, fetchOne } from '../crud'
import { getResult, postResult, putResult } from '../http'

export interface SelectOption {
    id: number
    label: string
    subLabel?: string
}

export function optionsToMap(options: SelectOption[]) {
    return options.reduce<Record<string, string>>((acc, item) => {
        acc[String(item.id)] = item.label
        return acc
    }, {})
}

export interface InventoryOption {
    id: number
    laboratoryId: number
    itemType: number
    itemId: number
    batchNo: string
    quantity: number
    lockedQuantity: number
    availableQuantity: number
    unitPrice: number
    productionDate?: string
    expiryDate?: string
    warningStatus: number
}

export interface EquipmentBorrowRecord {
    id: number
    equipmentId: number
    laboratoryId: number
    borrowerUserId: number
    approverUserId?: number
    purpose: string
    borrowDate: string
    dueDate: string
    actualReturnDate?: string
    borrowStatus: number
    returnCondition?: string
    remarks?: string
}

export interface ConsumableInboundRecord {
    id: number
    consumableId: number
    laboratoryId: number
    operatorUserId: number
    batchNo: string
    inboundType: number
    quantity: number
    unitPrice: number
    totalAmount: number
    supplierName?: string
    productionDate?: string
    expiryDate?: string
    inboundDate: string
    remarks?: string
}

export interface ConsumableOutboundRecord {
    id: number
    consumableId: number
    laboratoryId: number
    applicantUserId: number
    approverUserId?: number
    operatorUserId?: number
    batchNo: string
    outboundType: number
    quantity: number
    unitPrice: number
    totalAmount: number
    outboundDate: string
    purpose?: string
    outboundStatus: number
    remarks?: string
}

export interface HazardousUsageRecord {
    id: number
    hazardousMaterialId: number
    laboratoryId: number
    applicantUserId: number
    approverUserId?: number
    operatorUserId?: number
    actionType: number
    batchNo: string
    quantity: number
    remainingQuantity?: number
    usageDate: string
    purpose?: string
    projectName?: string
    witnessName?: string
    usageStatus: number
    remarks?: string
}

export interface EquipmentRepairRecord {
    id: number
    equipmentId: number
    laboratoryId: number
    reporterUserId: number
    repairUserId?: number
    faultDescription: string
    reportTime: string
    repairStartTime?: string
    repairEndTime?: string
    repairStatus: number
    repairCost?: number
    repairResult?: string
    remarks?: string
}

export interface EquipmentCalibrationRecord {
    id: number
    equipmentId: number
    laboratoryId: number
    calibrationUserId?: number
    certificateNo: string
    calibrationDate: string
    validUntil: string
    calibrationResult: number
    calibrationStatus: number
    remarks?: string
}

function toOptions<T extends Record<string, unknown>>(
    records: T[],
    labelBuilder: (record: T) => string,
    subLabelBuilder?: (record: T) => string | undefined,
): SelectOption[] {
    return records.map((record) => ({
        id: Number(record.id),
        label: labelBuilder(record),
        subLabel: subLabelBuilder?.(record),
    }))
}

export async function fetchLaboratoryOptions() {
    const result = await fetchPage<Record<string, unknown>>('/laboratories', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.laboratoryName), (item) => String(item.laboratoryCode))
}

export async function fetchUserOptions() {
    const result = await fetchPage<Record<string, unknown>>('/users', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.realName), (item) => String(item.username))
}

export async function fetchEquipmentOptions() {
    const result = await fetchPage<Record<string, unknown>>('/equipment', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.equipmentName), (item) => String(item.equipmentCode))
}

export async function fetchEquipmentCategoryOptions() {
    const result = await fetchPage<Record<string, unknown>>('/equipment-categories', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.categoryName), (item) => String(item.categoryCode))
}

export async function fetchConsumableOptions() {
    const result = await fetchPage<Record<string, unknown>>('/consumables', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.consumableName), (item) => String(item.consumableCode))
}

export async function fetchConsumableCategoryOptions() {
    const result = await fetchPage<Record<string, unknown>>('/consumable-categories', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.categoryName), (item) => String(item.categoryCode))
}

export async function fetchHazardousMaterialOptions() {
    const result = await fetchPage<Record<string, unknown>>('/hazardous-materials', { current: 1, pageSize: 100 })
    return toOptions(result.data.records, (item) => String(item.materialName), (item) => String(item.hazardousCode))
}

export async function fetchInventoryOptions(laboratoryId: number, itemType: number, itemId: number) {
    const result = await getResult<InventoryOption[]>('/inventory/options', { laboratoryId, itemType, itemId })
    return result.data
}

export async function fetchEquipmentBorrows(params: Record<string, unknown>) {
    return fetchPage<EquipmentBorrowRecord>('/equipment-borrows', params)
}

export async function createEquipmentBorrow(data: Partial<EquipmentBorrowRecord>) {
    return createOne('/equipment-borrows', data)
}

export async function returnEquipmentBorrow(id: number, data: { returnCondition: string; remarks?: string }) {
    return putResult<boolean>(`/equipment-borrows/${id}/return`, data)
}

export async function updateEquipmentBorrowStatus(
    id: number,
    data: { borrowStatus: number; returnCondition?: string; remarks?: string },
) {
    return putResult<EquipmentBorrowRecord>(`/equipment-borrows/${id}/status`, data)
}

export async function sendEquipmentBorrowOverdueReminder(id: number, data: { message?: string }) {
    return postResult<boolean>(`/equipment-borrows/${id}/overdue-reminder`, data)
}

export async function fetchConsumableInbounds(params: Record<string, unknown>) {
    return fetchPage<ConsumableInboundRecord>('/consumable-inbounds', params)
}

export async function createConsumableInbound(data: Partial<ConsumableInboundRecord>) {
    return createOne('/consumable-inbounds', data)
}

export async function fetchConsumableOutbounds(params: Record<string, unknown>) {
    return fetchPage<ConsumableOutboundRecord>('/consumable-outbounds', params)
}

export async function createConsumableOutbound(data: Partial<ConsumableOutboundRecord>) {
    return createOne('/consumable-outbounds', data)
}

export async function fetchHazardousUsages(params: Record<string, unknown>) {
    return fetchPage<HazardousUsageRecord>('/hazardous-usages', params)
}

export async function createHazardousUsage(data: Partial<HazardousUsageRecord>) {
    return createOne('/hazardous-usages', data)
}

export async function fetchBorrowDetail(id: number) {
    return fetchOne<EquipmentBorrowRecord>('/equipment-borrows', id)
}

export async function fetchEquipmentRepairs(params: Record<string, unknown>) {
    return fetchPage<EquipmentRepairRecord>('/equipment-repairs', params)
}

export async function createEquipmentRepair(data: Partial<EquipmentRepairRecord>) {
    return createOne('/equipment-repairs', data)
}

export async function updateEquipmentRepairStatus(id: number, data: Partial<EquipmentRepairRecord>) {
    return putResult<EquipmentRepairRecord>(`/equipment-repairs/${id}/status`, data)
}

export async function fetchEquipmentCalibrations(params: Record<string, unknown>) {
    return fetchPage<EquipmentCalibrationRecord>('/equipment-calibrations', params)
}

export async function createEquipmentCalibration(data: Partial<EquipmentCalibrationRecord>) {
    return createOne('/equipment-calibrations', data)
}

export async function confirmEquipmentCalibration(id: number, data: Partial<EquipmentCalibrationRecord>) {
    return putResult<EquipmentCalibrationRecord>(`/equipment-calibrations/${id}/confirm`, data)
}
