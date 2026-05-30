<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">设备维修</h2>
                <p class="page-subtitle">记录报修、维修处理与修复结果，并联动设备状态到维修中、可用或报废。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('equipment_repair:edit')" type="primary" @click="openCreate">新增维修</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="records">
                <el-table-column label="设备" min-width="160">
                    <template #default="{ row }">
                        {{ equipmentMap[String(row.equipmentId)] || row.equipmentId }}
                    </template>
                </el-table-column>
                <el-table-column label="报修人" min-width="120">
                    <template #default="{ row }">
                        {{ userMap[String(row.reporterUserId)] || row.reporterUserId }}
                    </template>
                </el-table-column>
                <el-table-column label="维修人员" min-width="120">
                    <template #default="{ row }">
                        {{ userMap[String(row.repairUserId)] || row.repairUserId || '-' }}
                    </template>
                </el-table-column>
                <el-table-column prop="faultDescription" label="故障描述" min-width="220" show-overflow-tooltip />
                <el-table-column prop="reportTime" label="报修时间" min-width="170" />
                <el-table-column prop="repairStatus" label="状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag :type="repairStatusType(row.repairStatus)">{{ repairStatusText(row.repairStatus) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="repairCost" label="维修费用" min-width="100" />
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="authStore.hasPermission('equipment_repair:edit')"
                            size="small"
                            type="primary"
                            plain
                            @click="openUpdate(row)"
                        >
                            更新状态
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="createVisible" title="新增维修记录" width="760px">
            <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="140px">
                <el-form-item label="实验室" prop="laboratoryId">
                    <el-select v-model="createForm.laboratoryId" style="width: 100%">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="设备" prop="equipmentId">
                    <el-select v-model="createForm.equipmentId" filterable style="width: 100%">
                        <el-option v-for="item in equipmentOptions" :key="item.id" :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="报修人" prop="reporterUserId">
                    <el-select v-model="createForm.reporterUserId" filterable style="width: 100%">
                        <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="故障描述" prop="faultDescription">
                    <el-input v-model="createForm.faultDescription" type="textarea" :rows="3" maxlength="500" show-word-limit />
                </el-form-item>
                <el-form-item label="报修时间" prop="reportTime">
                    <el-date-picker v-model="createForm.reportTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
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

        <el-dialog v-model="updateVisible" title="更新维修状态" width="760px">
            <el-form ref="updateFormRef" :model="updateForm" :rules="updateRules" label-width="140px">
                <el-form-item label="维修人员" prop="repairUserId">
                    <el-select v-model="updateForm.repairUserId" filterable style="width: 100%">
                        <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="维修状态" prop="repairStatus">
                    <el-radio-group v-model="updateForm.repairStatus">
                        <el-radio :value="2">维修中</el-radio>
                        <el-radio :value="3">已修复</el-radio>
                        <el-radio :value="4">无法修复</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="开始维修时间">
                    <el-date-picker v-model="updateForm.repairStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="完成维修时间">
                    <el-date-picker v-model="updateForm.repairEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="维修费用">
                    <el-input-number v-model="updateForm.repairCost" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="维修结果">
                    <el-input v-model="updateForm.repairResult" type="textarea" :rows="3" maxlength="500" show-word-limit />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="updateForm.remarks" type="textarea" :rows="2" maxlength="255" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="updateVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="submitUpdate">保存</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
    createEquipmentRepair,
    fetchEquipmentOptions,
    fetchEquipmentRepairs,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    updateEquipmentRepairStatus,
    type EquipmentRepairRecord,
    type SelectOption,
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const updateVisible = ref(false)
const currentId = ref<number>()
const records = ref<EquipmentRepairRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const equipmentMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const updateFormRef = ref<FormInstance>()
const authStore = useAuthStore()

const createForm = reactive({
    equipmentId: undefined as number | undefined,
    laboratoryId: undefined as number | undefined,
    reporterUserId: undefined as number | undefined,
    faultDescription: '',
    reportTime: '',
    remarks: '',
})

const updateForm = reactive({
    repairUserId: undefined as number | undefined,
    repairStatus: 2,
    repairStartTime: '',
    repairEndTime: '',
    repairCost: 0,
    repairResult: '',
    remarks: '',
})

const createRules: FormRules = {
    equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    reporterUserId: [{ required: true, message: '请选择报修人', trigger: 'change' }],
    faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
    reportTime: [{ required: true, message: '请选择报修时间', trigger: 'change' }],
}

const updateRules: FormRules = {
    repairUserId: [{ required: true, message: '请选择维修人员', trigger: 'change' }],
    repairStatus: [{ required: true, message: '请选择维修状态', trigger: 'change' }],
}

function repairStatusText(status: number) {
    if (status === 2) return '维修中'
    if (status === 3) return '已修复'
    if (status === 4) return '无法修复'
    return '待处理'
}

function repairStatusType(status: number) {
    if (status === 2) return 'warning'
    if (status === 3) return 'success'
    if (status === 4) return 'danger'
    return ''
}

function openCreate() {
    createVisible.value = true
}

function openUpdate(row: EquipmentRepairRecord) {
    currentId.value = row.id
    updateForm.repairUserId = row.repairUserId
    updateForm.repairStatus = row.repairStatus
    updateForm.repairStartTime = row.repairStartTime ?? ''
    updateForm.repairEndTime = row.repairEndTime ?? ''
    updateForm.repairCost = row.repairCost ?? 0
    updateForm.repairResult = row.repairResult ?? ''
    updateForm.remarks = row.remarks ?? ''
    updateVisible.value = true
}

async function loadRecords() {
    loading.value = true
    try {
        const result = await fetchEquipmentRepairs({ current: 1, pageSize: 20 })
        records.value = result.data.records
    } finally {
        loading.value = false
    }
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
    equipmentMap.value = optionsToMap(equipment)
    userMap.value = optionsToMap(users)
}

async function submitCreate() {
    const valid = await createFormRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        await createEquipmentRepair(createForm)
        ElMessage.success('维修记录已创建')
        createVisible.value = false
        await loadRecords()
    } finally {
        saving.value = false
    }
}

async function submitUpdate() {
    const valid = await updateFormRef.value?.validate().catch(() => false)
    if (!valid || !currentId.value) return
    saving.value = true
    try {
        await updateEquipmentRepairStatus(currentId.value, updateForm)
        ElMessage.success('维修状态已更新')
        updateVisible.value = false
        await loadRecords()
    } finally {
        saving.value = false
    }
}

onMounted(async () => {
    await Promise.all([loadOptions(), loadRecords()])
})
</script>
