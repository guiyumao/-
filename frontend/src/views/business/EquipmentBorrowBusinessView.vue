<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">设备借用流程</h2>
                <p class="page-subtitle">先登记借用，借出期间跟踪是否逾期，归还时记录设备情况；逾期记录可直接向借用账号发送催还通知。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('equipment_borrow:edit')" type="primary" @click="openCreateDialog">新增借用</el-button>
            </el-space>
        </div>

        <div class="borrow-flow">
            <div v-for="step in workflowSteps" :key="step.title" class="flow-step">
                <span class="flow-index">{{ step.index }}</span>
                <div>
                    <strong>{{ step.title }}</strong>
                    <p>{{ step.desc }}</p>
                </div>
            </div>
        </div>

        <el-row :gutter="20" style="margin-bottom: 20px">
            <el-col :span="8">
                <el-card class="card-panel" shadow="never">
                    <el-statistic title="当前借出" :value="borrowedCount" />
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card class="card-panel" shadow="never">
                    <el-statistic title="已归还记录" :value="returnedCount" />
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card class="card-panel" shadow="never">
                    <el-statistic title="逾期记录" :value="overdueCount" />
                </el-card>
            </el-col>
        </el-row>

        <el-card class="card-panel" shadow="never">
            <div class="table-head">
                <div class="table-head-title">
                    <h3>借用记录</h3>
                    <p>{{ recordsSummaryText }}</p>
                </div>
                <el-radio-group v-model="statusFilter" class="status-filter">
                    <el-radio-button v-for="item in statusFilterOptions" :key="item.value" :label="item.value">
                        {{ item.label }}
                    </el-radio-button>
                </el-radio-group>
            </div>

            <el-table v-loading="loading" :data="filteredRecords" style="width: 100%">
                <el-table-column label="设备" min-width="160">
                    <template #default="{ row }">
                        {{ equipmentMap[String(row.equipmentId)] || row.equipmentId }}
                    </template>
                </el-table-column>
                <el-table-column label="实验室" min-width="140">
                    <template #default="{ row }">
                        {{ laboratoryMap[String(row.laboratoryId)] || row.laboratoryId }}
                    </template>
                </el-table-column>
                <el-table-column label="借用人" min-width="140">
                    <template #default="{ row }">
                        {{ userMap[String(row.borrowerUserId)] || row.borrowerUserId }}
                    </template>
                </el-table-column>
                <el-table-column prop="purpose" label="借用用途" min-width="220" show-overflow-tooltip />
                <el-table-column label="借用时间" min-width="160">
                    <template #default="{ row }">{{ formatDateTime(row.borrowDate) }}</template>
                </el-table-column>
                <el-table-column label="应还时间" min-width="160">
                    <template #default="{ row }">{{ formatDateTime(row.dueDate) }}</template>
                </el-table-column>
                <el-table-column label="状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag :type="borrowStatusType(row.borrowStatus)">{{ borrowStatusText(row.borrowStatus) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="处理建议" min-width="190">
                    <template #default="{ row }">
                        <span :class="['action-hint', { urgent: row.borrowStatus === 4 }]">{{ borrowActionHint(row) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-space v-if="authStore.hasPermission('equipment_borrow:edit')" wrap>
                            <el-button v-if="canReturn(row)" size="small" type="primary" plain @click="openReturnDialog(row)">
                                登记归还
                            </el-button>
                            <el-button size="small" plain @click="openStatusDialog(row)">更新状态</el-button>
                            <el-button
                                v-if="row.borrowStatus === 4"
                                size="small"
                                type="danger"
                                plain
                                :loading="remindingId === row.id"
                                @click="sendReminder(row)"
                            >
                                催还
                            </el-button>
                        </el-space>
                        <span v-else style="color: var(--text-secondary)">无权限</span>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="createVisible" title="新增设备借用" width="760px">
            <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="140px">
                <el-form-item label="实验室" prop="laboratoryId">
                    <el-select v-model="createForm.laboratoryId" placeholder="请选择实验室" style="width: 100%">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="设备" prop="equipmentId">
                    <el-select v-model="createForm.equipmentId" filterable placeholder="请选择设备" style="width: 100%">
                        <el-option
                            v-for="item in equipmentOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="借用人" prop="borrowerUserId">
                    <el-select v-model="createForm.borrowerUserId" filterable placeholder="请选择借用人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="审批人" prop="approverUserId">
                    <el-select v-model="createForm.approverUserId" filterable placeholder="请选择审批人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="借用时间" prop="borrowDate">
                    <el-date-picker v-model="createForm.borrowDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="应还时间" prop="dueDate">
                    <el-date-picker v-model="createForm.dueDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="借用用途" prop="purpose">
                    <el-input v-model="createForm.purpose" type="textarea" :rows="3" maxlength="255" show-word-limit />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="createForm.remarks" type="textarea" :rows="2" maxlength="255" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="createVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
                </el-space>
            </template>
        </el-dialog>

        <el-dialog v-model="returnVisible" class="return-dialog" title="设备归还登记" width="620px">
            <p class="dialog-tip">确认归还会把这条借用记录改为“已归还”，并释放设备为可借用状态。</p>
            <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="140px">
                <el-form-item label="借用记录">
                    <el-input :model-value="selectedBorrowText" disabled />
                </el-form-item>
                <el-form-item label="归还情况" prop="returnCondition">
                    <el-input v-model="returnForm.returnCondition" placeholder="例如：完好归还 / 需维修检查" />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="returnForm.remarks" type="textarea" :rows="3" maxlength="255" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="returnVisible = false">取消</el-button>
                    <el-button type="primary" :loading="returning" @click="submitReturn">确认归还</el-button>
                </el-space>
            </template>
        </el-dialog>

        <el-dialog v-model="statusVisible" class="return-dialog" title="编辑借用情况" width="620px">
            <p class="dialog-tip">用于修正借用流转状态或补充情况说明；改为“已归还/已拒绝”会释放设备，改为“借出中/已逾期”会保持设备为借出。</p>
            <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="140px">
                <el-form-item label="借用记录">
                    <el-input :model-value="selectedBorrowText" disabled />
                </el-form-item>
                <el-form-item label="借用状态" prop="borrowStatus">
                    <el-select v-model="statusForm.borrowStatus" placeholder="请选择状态" style="width: 100%">
                        <el-option label="待审批" :value="1" />
                        <el-option label="借出中" :value="2" />
                        <el-option label="已归还" :value="3" />
                        <el-option label="已逾期" :value="4" />
                        <el-option label="已拒绝" :value="5" />
                    </el-select>
                </el-form-item>
                <el-form-item label="情况说明">
                    <el-input v-model="statusForm.returnCondition" placeholder="例如：设备完好 / 已联系借用人 / 等待维修确认" />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="statusForm.remarks" type="textarea" :rows="3" maxlength="255" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="statusVisible = false">取消</el-button>
                    <el-button type="primary" :loading="statusSaving" @click="submitStatus">保存</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
    createEquipmentBorrow,
    fetchBorrowDetail,
    fetchEquipmentBorrows,
    fetchEquipmentOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    returnEquipmentBorrow,
    sendEquipmentBorrowOverdueReminder,
    type EquipmentBorrowRecord,
    type SelectOption,
    updateEquipmentBorrowStatus,
} from '../../api/modules/business'
import { borrowStatusText, borrowStatusType, formatDateTime } from './EquipmentBorrowBusinessHelpers'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const returning = ref(false)
const statusSaving = ref(false)
const remindingId = ref<number>()
const createVisible = ref(false)
const returnVisible = ref(false)
const statusVisible = ref(false)
const statusFilter = ref(0)
const records = ref<EquipmentBorrowRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const laboratoryMap = ref<Record<string, string>>({})
const equipmentMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const returnFormRef = ref<FormInstance>()
const statusFormRef = ref<FormInstance>()
const currentBorrowId = ref<number>()
const authStore = useAuthStore()

const createForm = reactive({
    equipmentId: undefined as number | undefined,
    laboratoryId: undefined as number | undefined,
    borrowerUserId: undefined as number | undefined,
    approverUserId: undefined as number | undefined,
    purpose: '',
    borrowDate: '',
    dueDate: '',
    remarks: '',
})

const returnForm = reactive({
    returnCondition: '',
    remarks: '',
})

const statusForm = reactive({
    borrowStatus: 2,
    returnCondition: '',
    remarks: '',
})

const createRules: FormRules = {
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
    borrowerUserId: [{ required: true, message: '请选择借用人', trigger: 'change' }],
    approverUserId: [{ required: true, message: '请选择审批人', trigger: 'change' }],
    borrowDate: [{ required: true, message: '请选择借用时间', trigger: 'change' }],
    dueDate: [
        { required: true, message: '请选择应还时间', trigger: 'change' },
        {
            validator: (_, value, callback) => {
                if (value && createForm.borrowDate && value <= createForm.borrowDate) {
                    callback(new Error('应还时间必须晚于借用时间'))
                    return
                }
                callback()
            },
            trigger: 'change',
        },
    ],
    purpose: [{ required: true, message: '请输入借用用途', trigger: 'blur' }],
}

const returnRules: FormRules = {
    returnCondition: [{ required: true, message: '请输入归还情况', trigger: 'blur' }],
}

const statusRules: FormRules = {
    borrowStatus: [{ required: true, message: '请选择借用状态', trigger: 'change' }],
}

const workflowSteps = [
    { index: '1', title: '登记借用', desc: '选择设备、实验室、借用人和应还时间。' },
    { index: '2', title: '跟踪状态', desc: '借出中关注应还时间，逾期后进入催还处理。' },
    { index: '3', title: '归还闭环', desc: '登记归还情况后，设备恢复为可借用。' },
]

const statusFilterOptions = [
    { label: '全部', value: 0 },
    { label: '借出中', value: 2 },
    { label: '已逾期', value: 4 },
    { label: '已归还', value: 3 },
]

const borrowedCount = computed(() => records.value.filter((item) => item.borrowStatus === 2).length)
const returnedCount = computed(() => records.value.filter((item) => item.borrowStatus === 3).length)
const overdueCount = computed(() => records.value.filter((item) => item.borrowStatus === 4).length)
const filteredRecords = computed(() => {
    if (!statusFilter.value) {
        return records.value
    }
    return records.value.filter((item) => item.borrowStatus === statusFilter.value)
})
const recordsSummaryText = computed(() => {
    if (!statusFilter.value) {
        return `共 ${records.value.length} 条记录，优先处理 ${overdueCount.value} 条逾期记录。`
    }
    return `当前筛选：${borrowStatusText(statusFilter.value)}，共 ${filteredRecords.value.length} 条。`
})

const selectedBorrowText = computed(() => {
    const borrow = records.value.find((item) => item.id === currentBorrowId.value)
    if (!borrow) {
        return ''
    }
    const equipmentName = equipmentMap.value[String(borrow.equipmentId)] || borrow.equipmentId
    return `${equipmentName} / 应还 ${formatDateTime(borrow.dueDate)}`
})

function resetCreateForm() {
    createForm.equipmentId = undefined
    createForm.laboratoryId = undefined
    createForm.borrowerUserId = undefined
    createForm.approverUserId = undefined
    createForm.purpose = ''
    createForm.borrowDate = ''
    createForm.dueDate = ''
    createForm.remarks = ''
}

function openCreateDialog() {
    resetCreateForm()
    createVisible.value = true
}

function openReturnDialog(row: EquipmentBorrowRecord) {
    currentBorrowId.value = row.id
    returnForm.returnCondition = ''
    returnForm.remarks = ''
    returnVisible.value = true
}

function openStatusDialog(row: EquipmentBorrowRecord) {
    currentBorrowId.value = row.id
    statusForm.borrowStatus = row.borrowStatus
    statusForm.returnCondition = row.returnCondition || ''
    statusForm.remarks = row.remarks || ''
    statusVisible.value = true
}

function canReturn(row: EquipmentBorrowRecord) {
    return row.borrowStatus === 2 || row.borrowStatus === 4
}

function borrowActionHint(row: EquipmentBorrowRecord) {
    if (row.borrowStatus === 4) {
        return '已超过应还时间，建议催还或登记归还'
    }
    if (row.borrowStatus === 2) {
        return '等待归还，到期前保持跟踪'
    }
    if (row.borrowStatus === 3) {
        return '流程已闭环，可查看归还情况'
    }
    if (row.borrowStatus === 5) {
        return '申请已拒绝，设备应保持可用'
    }
    return '待审批或待处理'
}

async function loadOptions() {
    const [labs, equipment, users] = await Promise.all([
        fetchLaboratoryOptions(),
        fetchEquipmentOptions(),
        fetchUserOptions(),
    ])
    laboratoryOptions.value = labs
    equipmentOptions.value = equipment
    userOptions.value = users
    laboratoryMap.value = optionsToMap(labs)
    equipmentMap.value = optionsToMap(equipment)
    userMap.value = optionsToMap(users)
}

async function loadRecords() {
    loading.value = true
    try {
        const result = await fetchEquipmentBorrows({ current: 1, pageSize: 20 })
        records.value = result.data.records
    } finally {
        loading.value = false
    }
}

async function submitCreate() {
    const valid = await createFormRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    saving.value = true
    try {
        await createEquipmentBorrow(createForm)
        ElMessage.success('借用申请已提交')
        createVisible.value = false
        await loadRecords()
    } finally {
        saving.value = false
    }
}

async function submitReturn() {
    const valid = await returnFormRef.value?.validate().catch(() => false)
    if (!valid || !currentBorrowId.value) {
        return
    }
    returning.value = true
    try {
        await fetchBorrowDetail(currentBorrowId.value)
        await returnEquipmentBorrow(currentBorrowId.value, returnForm)
        ElMessage.success('设备已归还')
        returnVisible.value = false
        await loadRecords()
    } finally {
        returning.value = false
    }
}

async function submitStatus() {
    const valid = await statusFormRef.value?.validate().catch(() => false)
    if (!valid || !currentBorrowId.value) {
        return
    }
    statusSaving.value = true
    try {
        await updateEquipmentBorrowStatus(currentBorrowId.value, statusForm)
        ElMessage.success('借用情况已更新')
        statusVisible.value = false
        await loadRecords()
    } finally {
        statusSaving.value = false
    }
}

async function sendReminder(row: EquipmentBorrowRecord) {
    const equipmentName = equipmentMap.value[String(row.equipmentId)] || row.equipmentId
    const borrowerName = userMap.value[String(row.borrowerUserId)] || row.borrowerUserId
    const confirmed = await ElMessageBox.confirm(
        `将向“${borrowerName}”发送设备“${equipmentName}”的逾期催还通知，通知会出现在该账号的“到期提醒 / 我的通知”中。`,
        '确认发送催还',
        {
            confirmButtonText: '发送催还',
            cancelButtonText: '取消',
            type: 'warning',
        },
    ).catch(() => false)
    if (!confirmed) {
        return
    }
    remindingId.value = row.id
    try {
        await sendEquipmentBorrowOverdueReminder(row.id, {
            message: `您借用的设备“${equipmentName}”已超过应还时间（${formatDateTime(row.dueDate)}），请尽快归还或联系管理员处理。`,
        })
        ElMessage.success(`已向 ${borrowerName} 发送催还通知`)
    } finally {
        remindingId.value = undefined
    }
}

onMounted(async () => {
    await Promise.all([loadOptions(), loadRecords()])
})
</script>

<style scoped>
.borrow-flow {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 14px;
    margin-bottom: 20px;
}

.flow-step {
    display: flex;
    gap: 12px;
    min-height: 92px;
    padding: 18px;
    border: 1px solid rgba(21, 49, 59, 0.1);
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.68);
    box-shadow: 0 14px 32px rgba(17, 39, 47, 0.08);
}

.flow-index {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    flex: 0 0 28px;
    border-radius: 999px;
    background: rgba(18, 127, 114, 0.12);
    color: var(--accent-deep);
    font-weight: 800;
}

.flow-step strong {
    color: var(--text-main);
    font-weight: 800;
}

.flow-step p,
.dialog-tip {
    margin: 6px 0 0;
    color: var(--text-secondary);
    line-height: 1.65;
}

.status-filter {
    flex-shrink: 0;
}

.action-hint {
    color: var(--text-secondary);
    line-height: 1.6;
}

.action-hint.urgent {
    color: var(--danger);
    font-weight: 700;
}

.dialog-tip {
    margin: 0 0 18px;
    padding: 12px 14px;
    border-radius: 14px;
    background: rgba(18, 127, 114, 0.08);
    border: 1px solid rgba(18, 127, 114, 0.12);
}

.return-dialog :deep(.el-dialog) {
    background: rgba(255, 250, 244, 0.99);
    border: 1px solid rgba(21, 49, 59, 0.12);
    box-shadow: 0 28px 60px rgba(17, 39, 47, 0.2);
}

.return-dialog :deep(.el-dialog__header) {
    padding: 22px 24px 12px;
    border-bottom: 1px solid rgba(21, 49, 59, 0.08);
}

.return-dialog :deep(.el-dialog__title) {
    color: var(--text-main);
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
}

.return-dialog :deep(.el-dialog__body) {
    padding: 24px;
    color: var(--text-main);
}

.return-dialog :deep(.el-dialog__footer) {
    padding: 12px 24px 24px;
    border-top: 1px solid rgba(21, 49, 59, 0.08);
}

.return-dialog :deep(.el-form-item__label) {
    color: var(--text-main);
    font-weight: 700;
}

.return-dialog :deep(.el-input__wrapper),
.return-dialog :deep(.el-textarea__inner) {
    background: rgba(255, 255, 255, 0.96);
    border: 1px solid rgba(21, 49, 59, 0.14);
    color: var(--text-main);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.55);
}

.return-dialog :deep(.el-input__wrapper.is-focus),
.return-dialog :deep(.el-textarea__inner:focus) {
    border-color: rgba(18, 127, 114, 0.45);
    box-shadow:
        0 0 0 3px rgba(18, 127, 114, 0.12),
        inset 0 1px 0 rgba(255, 255, 255, 0.55);
}

.return-dialog :deep(.el-input__inner),
.return-dialog :deep(.el-textarea__inner) {
    color: var(--text-main);
}

.return-dialog :deep(.el-input__inner::placeholder),
.return-dialog :deep(.el-textarea__inner::placeholder) {
    color: #6c7d86;
}

.return-dialog :deep(.el-input.is-disabled .el-input__wrapper) {
    background: rgba(242, 236, 228, 0.96);
    border-color: rgba(21, 49, 59, 0.12);
    box-shadow: none;
}

.return-dialog :deep(.el-input.is-disabled .el-input__inner) {
    color: #314a54;
    -webkit-text-fill-color: #314a54;
}

@media (max-width: 1100px) {
    .borrow-flow {
        grid-template-columns: 1fr;
    }
}
</style>
