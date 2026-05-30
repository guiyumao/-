<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">危化品业务流程</h2>
                <p class="page-subtitle">危化品入库、领用、归还和废液处理统一登记，按批次校验库存并记录项目与见证人信息。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('hazardous_usage:edit')" type="primary" @click="dialogVisible = true">新增记录</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never" style="margin-bottom: 20px">
            <el-row :gutter="20">
                <el-col :span="8"><el-statistic title="业务记录数" :value="records.length" /></el-col>
                <el-col :span="8"><el-statistic title="领用/处置数量" :value="issueQuantity" /></el-col>
                <el-col :span="8"><el-statistic title="归还数量" :value="returnQuantity" /></el-col>
            </el-row>
        </el-card>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="records">
                <el-table-column label="危化品" min-width="160">
                    <template #default="{ row }">
                        {{ materialMap[String(row.hazardousMaterialId)] || row.hazardousMaterialId }}
                    </template>
                </el-table-column>
                <el-table-column prop="actionType" label="业务动作" min-width="110">
                    <template #default="{ row }">{{ actionTypeText(row.actionType) }}</template>
                </el-table-column>
                <el-table-column prop="batchNo" label="批号" min-width="150" />
                <el-table-column prop="quantity" label="数量" min-width="100" />
                <el-table-column prop="remainingQuantity" label="剩余数量" min-width="110" />
                <el-table-column prop="usageDate" label="操作时间" min-width="170" />
                <el-table-column prop="projectName" label="项目名称" min-width="180" show-overflow-tooltip />
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" title="新增危化品业务记录" width="840px">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
                <el-form-item label="实验室" prop="laboratoryId">
                    <el-select v-model="form.laboratoryId" placeholder="请选择实验室" style="width: 100%" @change="handleInventoryDependencyChange">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="危化品" prop="hazardousMaterialId">
                    <el-select v-model="form.hazardousMaterialId" filterable placeholder="请选择危化品" style="width: 100%" @change="handleInventoryDependencyChange">
                        <el-option
                            v-for="item in materialOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="业务类型" prop="actionType">
                    <el-radio-group v-model="form.actionType" @change="handleActionTypeChange">
                        <el-radio :value="1">入库</el-radio>
                        <el-radio :value="2">领用</el-radio>
                        <el-radio :value="3">归还</el-radio>
                        <el-radio :value="4">废液处理</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="库存批次" prop="batchNo">
                    <el-select
                        v-model="form.batchNo"
                        :disabled="form.actionType === 1"
                        placeholder="请选择批次"
                        style="width: 100%"
                        @change="handleBatchChange"
                    >
                        <el-option
                            v-for="item in inventoryOptions"
                            :key="item.id"
                            :label="`${item.batchNo} / 可用 ${item.availableQuantity}`"
                            :value="item.batchNo"
                        />
                    </el-select>
                    <div v-if="form.actionType === 1" style="color: var(--text-secondary); font-size: 12px; margin-top: 6px">
                        入库场景请直接填写新的批次号。
                    </div>
                </el-form-item>
                <el-form-item v-if="form.actionType === 1" label="新批号" prop="batchNo">
                    <el-input v-model="form.batchNo" placeholder="请输入新批号" />
                </el-form-item>
                <el-form-item label="申请人" prop="applicantUserId">
                    <el-select v-model="form.applicantUserId" filterable placeholder="请选择申请人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="审批人">
                    <el-select v-model="form.approverUserId" filterable placeholder="请选择审批人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="操作人">
                    <el-select v-model="form.operatorUserId" filterable placeholder="请选择操作人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="数量" prop="quantity">
                    <el-input-number v-model="form.quantity" :min="0.01" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="可用数量">
                    <el-input :model-value="selectedInventory ? String(selectedInventory.availableQuantity) : '-'" disabled />
                </el-form-item>
                <el-form-item label="剩余数量">
                    <el-input-number v-model="form.remainingQuantity" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="操作时间" prop="usageDate">
                    <el-date-picker v-model="form.usageDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="用途" prop="purpose">
                    <el-input v-model="form.purpose" type="textarea" :rows="2" maxlength="255" show-word-limit />
                </el-form-item>
                <el-form-item label="项目名称">
                    <el-input v-model="form.projectName" />
                </el-form-item>
                <el-form-item label="见证人">
                    <el-input v-model="form.witnessName" />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="form.remarks" type="textarea" :rows="2" maxlength="255" show-word-limit />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="submitForm">提交</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
    createHazardousUsage,
    fetchHazardousMaterialOptions,
    fetchHazardousUsages,
    fetchInventoryOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    type HazardousUsageRecord,
    type InventoryOption,
    type SelectOption,
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const records = ref<HazardousUsageRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const materialOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const inventoryOptions = ref<InventoryOption[]>([])
const materialMap = ref<Record<string, string>>({})
const formRef = ref<FormInstance>()
const authStore = useAuthStore()

const form = reactive({
    hazardousMaterialId: undefined as number | undefined,
    laboratoryId: undefined as number | undefined,
    applicantUserId: undefined as number | undefined,
    approverUserId: undefined as number | undefined,
    operatorUserId: undefined as number | undefined,
    actionType: 2,
    batchNo: '',
    quantity: 1,
    remainingQuantity: 0,
    usageDate: '',
    purpose: '',
    projectName: '',
    witnessName: '',
    remarks: '',
})

const selectedInventory = computed(() => inventoryOptions.value.find((item) => item.batchNo === form.batchNo))
const issueQuantity = computed(() => records.value.filter((item) => item.actionType === 2 || item.actionType === 4).reduce((sum, item) => sum + Number(item.quantity ?? 0), 0))
const returnQuantity = computed(() => records.value.filter((item) => item.actionType === 3).reduce((sum, item) => sum + Number(item.quantity ?? 0), 0))

const rules: FormRules = {
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    hazardousMaterialId: [{ required: true, message: '请选择危化品', trigger: 'change' }],
    batchNo: [{ required: true, message: '请填写或选择批号', trigger: 'change' }],
    applicantUserId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
    quantity: [
        { required: true, message: '请输入数量', trigger: 'change' },
        {
            validator: (_, value, callback) => {
                if (form.actionType !== 1 && selectedInventory.value && Number(value) > Number(selectedInventory.value.availableQuantity)) {
                    callback(new Error('数量不能超过当前危化品可用库存'))
                    return
                }
                callback()
            },
            trigger: 'change',
        },
    ],
    usageDate: [{ required: true, message: '请选择操作时间', trigger: 'change' }],
    purpose: [{ required: true, message: '请输入业务用途', trigger: 'blur' }],
}

function actionTypeText(actionType: number) {
    if (actionType === 1) {
        return '入库'
    }
    if (actionType === 2) {
        return '领用'
    }
    if (actionType === 3) {
        return '归还'
    }
    return '废液处理'
}

async function loadRecords() {
    loading.value = true
    try {
        const result = await fetchHazardousUsages({ current: 1, pageSize: 20 })
        records.value = result.data.records
    } finally {
        loading.value = false
    }
}

async function loadOptions() {
    const [labs, materials, users] = await Promise.all([
        fetchLaboratoryOptions(),
        fetchHazardousMaterialOptions(),
        fetchUserOptions(),
    ])
    laboratoryOptions.value = labs
    materialOptions.value = materials
    userOptions.value = users
    materialMap.value = optionsToMap(materials)
}

async function handleInventoryDependencyChange() {
    form.batchNo = ''
    inventoryOptions.value = []
    if (form.actionType === 1 || !form.laboratoryId || !form.hazardousMaterialId) {
        return
    }
    inventoryOptions.value = await fetchInventoryOptions(form.laboratoryId, 3, form.hazardousMaterialId)
}

function handleBatchChange() {
    form.remainingQuantity = selectedInventory.value ? Number(selectedInventory.value.availableQuantity) : 0
}

async function handleActionTypeChange() {
    form.batchNo = ''
    form.remainingQuantity = 0
    await handleInventoryDependencyChange()
}

function resetForm() {
    form.hazardousMaterialId = undefined
    form.laboratoryId = undefined
    form.applicantUserId = undefined
    form.approverUserId = undefined
    form.operatorUserId = undefined
    form.actionType = 2
    form.batchNo = ''
    form.quantity = 1
    form.remainingQuantity = 0
    form.usageDate = ''
    form.purpose = ''
    form.projectName = ''
    form.witnessName = ''
    form.remarks = ''
    inventoryOptions.value = []
}

async function submitForm() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    saving.value = true
    try {
        await createHazardousUsage(form)
        ElMessage.success('危化品业务记录已创建')
        dialogVisible.value = false
        resetForm()
        await loadRecords()
    } finally {
        saving.value = false
    }
}

onMounted(async () => {
    await Promise.all([loadOptions(), loadRecords()])
})
</script>
