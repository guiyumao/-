<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">设备校准</h2>
                <p class="page-subtitle">登记校准任务、证书编号与确认结果，并自动刷新设备的上下次校准日期。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('equipment_calibration:edit')" type="primary" @click="openCreate">新增校准</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="records">
                <el-table-column label="设备" min-width="160">
                    <template #default="{ row }">
                        {{ equipmentMap[String(row.equipmentId)] || row.equipmentId }}
                    </template>
                </el-table-column>
                <el-table-column prop="certificateNo" label="证书编号" min-width="180" />
                <el-table-column prop="calibrationDate" label="校准日期" min-width="170" />
                <el-table-column prop="validUntil" label="有效期至" min-width="170" />
                <el-table-column prop="calibrationResult" label="结果" min-width="100">
                    <template #default="{ row }">{{ row.calibrationResult === 1 ? '合格' : '不合格' }}</template>
                </el-table-column>
                <el-table-column prop="calibrationStatus" label="状态" min-width="100">
                    <template #default="{ row }">{{ row.calibrationStatus === 2 ? '已确认' : '待确认' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="authStore.hasPermission('equipment_calibration:edit')"
                            size="small"
                            type="primary"
                            plain
                            @click="openConfirm(row)"
                        >
                            确认校准
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="createVisible" title="新增校准任务" width="760px">
            <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="150px">
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
                <el-form-item label="证书编号" prop="certificateNo">
                    <el-input v-model="createForm.certificateNo" />
                </el-form-item>
                <el-form-item label="校准日期" prop="calibrationDate">
                    <el-date-picker v-model="createForm.calibrationDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="有效期至" prop="validUntil">
                    <el-date-picker v-model="createForm.validUntil" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
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

        <el-dialog v-model="confirmVisible" title="确认校准结果" width="760px">
            <el-form ref="confirmFormRef" :model="confirmForm" :rules="confirmRules" label-width="150px">
                <el-form-item label="校准人员" prop="calibrationUserId">
                    <el-select v-model="confirmForm.calibrationUserId" filterable style="width: 100%">
                        <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="校准日期" prop="calibrationDate">
                    <el-date-picker v-model="confirmForm.calibrationDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="有效期至" prop="validUntil">
                    <el-date-picker v-model="confirmForm.validUntil" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="校准结果" prop="calibrationResult">
                    <el-radio-group v-model="confirmForm.calibrationResult">
                        <el-radio :value="1">合格</el-radio>
                        <el-radio :value="2">不合格</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="confirmForm.remarks" type="textarea" :rows="2" maxlength="255" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="confirmVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="submitConfirm">确认</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
    confirmEquipmentCalibration,
    createEquipmentCalibration,
    fetchEquipmentCalibrations,
    fetchEquipmentOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    type EquipmentCalibrationRecord,
    type SelectOption,
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const confirmVisible = ref(false)
const currentId = ref<number>()
const records = ref<EquipmentCalibrationRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const equipmentMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const confirmFormRef = ref<FormInstance>()
const authStore = useAuthStore()

const createForm = reactive({
    equipmentId: undefined as number | undefined,
    laboratoryId: undefined as number | undefined,
    certificateNo: '',
    calibrationDate: '',
    validUntil: '',
    remarks: '',
})

const confirmForm = reactive({
    calibrationUserId: undefined as number | undefined,
    calibrationDate: '',
    validUntil: '',
    calibrationResult: 1,
    calibrationStatus: 2,
    remarks: '',
})

const createRules: FormRules = {
    equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    certificateNo: [{ required: true, message: '请输入证书编号', trigger: 'blur' }],
    calibrationDate: [{ required: true, message: '请选择校准日期', trigger: 'change' }],
    validUntil: [{ required: true, message: '请选择有效期', trigger: 'change' }],
}

const confirmRules: FormRules = {
    calibrationUserId: [{ required: true, message: '请选择校准人员', trigger: 'change' }],
    calibrationDate: [{ required: true, message: '请选择校准日期', trigger: 'change' }],
    validUntil: [{ required: true, message: '请选择有效期', trigger: 'change' }],
    calibrationResult: [{ required: true, message: '请选择校准结果', trigger: 'change' }],
}

function openCreate() {
    createVisible.value = true
}

function openConfirm(row: EquipmentCalibrationRecord) {
    currentId.value = row.id
    confirmForm.calibrationUserId = row.calibrationUserId
    confirmForm.calibrationDate = row.calibrationDate
    confirmForm.validUntil = row.validUntil
    confirmForm.calibrationResult = row.calibrationResult ?? 1
    confirmForm.remarks = row.remarks ?? ''
    confirmVisible.value = true
}

async function loadRecords() {
    loading.value = true
    try {
        const result = await fetchEquipmentCalibrations({ current: 1, pageSize: 20 })
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
}

async function submitCreate() {
    const valid = await createFormRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        await createEquipmentCalibration(createForm)
        ElMessage.success('校准任务已创建')
        createVisible.value = false
        await loadRecords()
    } finally {
        saving.value = false
    }
}

async function submitConfirm() {
    const valid = await confirmFormRef.value?.validate().catch(() => false)
    if (!valid || !currentId.value) return
    saving.value = true
    try {
        await confirmEquipmentCalibration(currentId.value, confirmForm)
        ElMessage.success('校准结果已确认')
        confirmVisible.value = false
        await loadRecords()
    } finally {
        saving.value = false
    }
}

onMounted(async () => {
    await Promise.all([loadOptions(), loadRecords()])
})
</script>
