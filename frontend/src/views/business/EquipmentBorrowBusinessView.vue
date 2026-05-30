<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">设备借用流程</h2>
                <p class="page-subtitle">提交设备借用、跟踪归还状态，并在归还时同步登记设备完好情况。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('equipment_borrow:edit')" type="primary" @click="openCreateDialog">新增借用</el-button>
            </el-space>
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
            <el-table v-loading="loading" :data="records" style="width: 100%">
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
                <el-table-column label="操作" width="140" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="row.borrowStatus !== 3 && authStore.hasPermission('equipment_borrow:edit')"
                            size="small"
                            type="primary"
                            plain
                            @click="openReturnDialog(row)"
                        >
                            归还登记
                        </el-button>
                        <span v-else style="color: var(--text-secondary)">已完成</span>
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

        <el-dialog v-model="returnVisible" title="设备归还登记" width="620px">
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
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
    createEquipmentBorrow,
    fetchBorrowDetail,
    fetchEquipmentBorrows,
    fetchEquipmentOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    returnEquipmentBorrow,
    type EquipmentBorrowRecord,
    type SelectOption,
} from '../../api/modules/business'
import { borrowStatusText, borrowStatusType, formatDateTime } from './EquipmentBorrowBusinessHelpers'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const returning = ref(false)
const createVisible = ref(false)
const returnVisible = ref(false)
const records = ref<EquipmentBorrowRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const laboratoryMap = ref<Record<string, string>>({})
const equipmentMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const returnFormRef = ref<FormInstance>()
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

const borrowedCount = computed(() => records.value.filter((item) => item.borrowStatus === 2).length)
const returnedCount = computed(() => records.value.filter((item) => item.borrowStatus === 3).length)
const overdueCount = computed(() => records.value.filter((item) => item.borrowStatus === 4).length)

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

onMounted(async () => {
    await Promise.all([loadOptions(), loadRecords()])
})
</script>
