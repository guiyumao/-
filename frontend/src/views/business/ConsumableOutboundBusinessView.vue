<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">耗材出库</h2>
                <p class="page-subtitle">按实验室与耗材联动可用批次，自动带出单价并校验可用库存。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('consumable_outbound:edit')" type="primary" @click="dialogVisible = true">新增出库</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never" style="margin-bottom: 20px">
            <el-row :gutter="20">
                <el-col :span="8"><el-statistic title="出库记录数" :value="records.length" /></el-col>
                <el-col :span="8"><el-statistic title="出库数量" :value="totalQuantity" /></el-col>
                <el-col :span="8"><el-statistic title="出库金额" :value="totalAmount" prefix="¥" /></el-col>
            </el-row>
        </el-card>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="records">
                <el-table-column label="耗材" min-width="160">
                    <template #default="{ row }">
                        {{ consumableMap[String(row.consumableId)] || row.consumableId }}
                    </template>
                </el-table-column>
                <el-table-column prop="batchNo" label="批号" min-width="150" />
                <el-table-column prop="quantity" label="数量" min-width="100" />
                <el-table-column prop="unitPrice" label="单价" min-width="100" />
                <el-table-column prop="totalAmount" label="金额" min-width="110" />
                <el-table-column prop="outboundDate" label="出库时间" min-width="170" />
                <el-table-column prop="purpose" label="用途" min-width="200" show-overflow-tooltip />
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" title="新增耗材出库" width="820px">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
                <el-form-item label="实验室" prop="laboratoryId">
                    <el-select v-model="form.laboratoryId" placeholder="请选择实验室" style="width: 100%" @change="handleInventoryDependencyChange">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="耗材" prop="consumableId">
                    <el-select v-model="form.consumableId" filterable placeholder="请选择耗材" style="width: 100%" @change="handleInventoryDependencyChange">
                        <el-option
                            v-for="item in consumableOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="库存批次" prop="batchNo">
                    <el-select v-model="form.batchNo" placeholder="请选择可用批次" style="width: 100%" @change="handleBatchChange">
                        <el-option
                            v-for="item in inventoryOptions"
                            :key="item.id"
                            :label="`${item.batchNo} / 可用 ${item.availableQuantity}`"
                            :value="item.batchNo"
                        />
                    </el-select>
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
                <el-form-item label="审批人" prop="approverUserId">
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
                <el-form-item label="单价">
                    <el-input :model-value="Number(form.unitPrice).toFixed(2)" disabled />
                </el-form-item>
                <el-form-item label="可用数量">
                    <el-input :model-value="selectedInventory ? String(selectedInventory.availableQuantity) : '-'" disabled />
                </el-form-item>
                <el-form-item label="出库类型" prop="outboundType">
                    <el-radio-group v-model="form.outboundType">
                        <el-radio :value="1">申领</el-radio>
                        <el-radio :value="2">实验使用</el-radio>
                        <el-radio :value="3">调拨</el-radio>
                        <el-radio :value="4">损耗</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="出库时间" prop="outboundDate">
                    <el-date-picker v-model="form.outboundDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="用途" prop="purpose">
                    <el-input v-model="form.purpose" type="textarea" :rows="3" maxlength="255" show-word-limit />
                </el-form-item>
                <el-form-item label="预计金额">
                    <el-input :model-value="estimatedAmountText" disabled />
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
    createConsumableOutbound,
    fetchConsumableOptions,
    fetchConsumableOutbounds,
    fetchInventoryOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    type ConsumableOutboundRecord,
    type InventoryOption,
    type SelectOption,
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const records = ref<ConsumableOutboundRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const consumableOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const inventoryOptions = ref<InventoryOption[]>([])
const consumableMap = ref<Record<string, string>>({})
const formRef = ref<FormInstance>()
const authStore = useAuthStore()

const form = reactive({
    consumableId: undefined as number | undefined,
    laboratoryId: undefined as number | undefined,
    applicantUserId: undefined as number | undefined,
    approverUserId: undefined as number | undefined,
    operatorUserId: undefined as number | undefined,
    batchNo: '',
    outboundType: 1,
    quantity: 1,
    unitPrice: 0,
    outboundDate: '',
    purpose: '',
    remarks: '',
})

const selectedInventory = computed(() => inventoryOptions.value.find((item) => item.batchNo === form.batchNo))
const totalQuantity = computed(() => records.value.reduce((sum, item) => sum + Number(item.quantity ?? 0), 0))
const totalAmount = computed(() => records.value.reduce((sum, item) => sum + Number(item.totalAmount ?? 0), 0).toFixed(2))
const estimatedAmountText = computed(() => `¥${(Number(form.quantity) * Number(form.unitPrice)).toFixed(2)}`)

const rules: FormRules = {
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    consumableId: [{ required: true, message: '请选择耗材', trigger: 'change' }],
    batchNo: [{ required: true, message: '请选择库存批次', trigger: 'change' }],
    applicantUserId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
    approverUserId: [{ required: true, message: '请选择审批人', trigger: 'change' }],
    quantity: [
        { required: true, message: '请输入数量', trigger: 'change' },
        {
            validator: (_, value, callback) => {
                if (selectedInventory.value && Number(value) > Number(selectedInventory.value.availableQuantity)) {
                    callback(new Error('出库数量不能超过可用库存'))
                    return
                }
                callback()
            },
            trigger: 'change',
        },
    ],
    outboundDate: [{ required: true, message: '请选择出库时间', trigger: 'change' }],
    purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }],
}

async function loadRecords() {
    loading.value = true
    try {
        const result = await fetchConsumableOutbounds({ current: 1, pageSize: 20 })
        records.value = result.data.records
    } finally {
        loading.value = false
    }
}

async function loadOptions() {
    const [labs, consumables, users] = await Promise.all([
        fetchLaboratoryOptions(),
        fetchConsumableOptions(),
        fetchUserOptions(),
    ])
    laboratoryOptions.value = labs
    consumableOptions.value = consumables
    userOptions.value = users
    consumableMap.value = optionsToMap(consumables)
}

async function handleInventoryDependencyChange() {
    form.batchNo = ''
    form.unitPrice = 0
    inventoryOptions.value = []
    if (!form.laboratoryId || !form.consumableId) {
        return
    }
    inventoryOptions.value = await fetchInventoryOptions(form.laboratoryId, 2, form.consumableId)
}

function handleBatchChange() {
    form.unitPrice = selectedInventory.value?.unitPrice ?? 0
}

function resetForm() {
    form.consumableId = undefined
    form.laboratoryId = undefined
    form.applicantUserId = undefined
    form.approverUserId = undefined
    form.operatorUserId = undefined
    form.batchNo = ''
    form.outboundType = 1
    form.quantity = 1
    form.unitPrice = 0
    form.outboundDate = ''
    form.purpose = ''
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
        await createConsumableOutbound(form)
        ElMessage.success('出库记录已创建')
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
