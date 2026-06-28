<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">耗材入库</h2>
                <p class="page-subtitle">登记采购入库批次、单价和有效期，并实时形成库存批次台账。</p>
            </div>
            <el-space>
                <el-button @click="loadRecords">刷新</el-button>
                <el-button v-if="authStore.hasPermission('consumable_inbound:edit')" type="primary" @click="dialogVisible = true">新增入库</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never" style="margin-bottom: 20px">
            <el-row :gutter="20">
                <el-col :span="8"><el-statistic title="入库记录数" :value="records.length" /></el-col>
                <el-col :span="8"><el-statistic title="入库数量" :value="totalQuantity" /></el-col>
                <el-col :span="8"><el-statistic title="入库金额" :value="totalAmount" prefix="￥" /></el-col>
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
                <el-table-column prop="supplierName" label="供应商" min-width="150" />
                <el-table-column prop="inboundDate" label="入库时间" min-width="170" />
                <el-table-column label="来源信息" min-width="220">
                    <template #default="{ row }">
                        <div class="data-stack">
                            <strong>操作人：{{ userMap[String(row.operatorUserId)] || row.operatorUserId || '-' }}</strong>
                            <span>供应商：{{ row.supplierName || '-' }}</span>
                            <span>批号：{{ row.batchNo || '-' }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="remarks" label="来源说明" min-width="180" show-overflow-tooltip />
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" title="新增耗材入库" width="780px">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
                <el-form-item label="实验室" prop="laboratoryId">
                    <el-select v-model="form.laboratoryId" placeholder="请选择实验室" style="width: 100%">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="耗材" prop="consumableId">
                    <el-select v-model="form.consumableId" filterable placeholder="请选择耗材" style="width: 100%">
                        <el-option
                            v-for="item in consumableOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="操作人" prop="operatorUserId">
                    <el-select v-model="form.operatorUserId" filterable placeholder="请选择操作人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label} (${item.subLabel})` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="批号" prop="batchNo">
                    <el-input v-model="form.batchNo" />
                </el-form-item>
                <el-form-item label="入库类型" prop="inboundType">
                    <el-radio-group v-model="form.inboundType">
                        <el-radio :value="1">采购</el-radio>
                        <el-radio :value="2">调拨</el-radio>
                        <el-radio :value="3">退回</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="数量" prop="quantity">
                    <el-input-number v-model="form.quantity" :min="0.01" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="单价" prop="unitPrice">
                    <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="入库时间" prop="inboundDate">
                    <el-date-picker v-model="form.inboundDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="生产日期">
                    <el-date-picker v-model="form.productionDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="过期日期">
                    <el-date-picker v-model="form.expiryDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="供应商">
                    <el-input v-model="form.supplierName" />
                </el-form-item>
                <el-form-item label="预估金额">
                    <el-input :model-value="estimatedAmountText" disabled />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="form.remarks" type="textarea" :rows="3" maxlength="255" show-word-limit />
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
    createConsumableInbound,
    fetchConsumableInbounds,
    fetchConsumableOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    type ConsumableInboundRecord,
    type SelectOption,
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const records = ref<ConsumableInboundRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const consumableOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const consumableMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const formRef = ref<FormInstance>()
const authStore = useAuthStore()

const form = reactive({
    consumableId: undefined as number | undefined,
    laboratoryId: undefined as number | undefined,
    operatorUserId: undefined as number | undefined,
    batchNo: '',
    inboundType: 1,
    quantity: 1,
    unitPrice: 0,
    supplierName: '',
    inboundDate: '',
    productionDate: '',
    expiryDate: '',
    remarks: '',
})

const rules: FormRules = {
    consumableId: [{ required: true, message: '请选择耗材', trigger: 'change' }],
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    operatorUserId: [{ required: true, message: '请选择操作人', trigger: 'change' }],
    batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
    inboundDate: [{ required: true, message: '请选择入库时间', trigger: 'change' }],
    quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
    unitPrice: [{ required: true, message: '请输入单价', trigger: 'change' }],
}

const totalQuantity = computed(() => records.value.reduce((sum, item) => sum + Number(item.quantity ?? 0), 0))
const totalAmount = computed(() => records.value.reduce((sum, item) => sum + Number(item.totalAmount ?? 0), 0).toFixed(2))
const estimatedAmountText = computed(() => `￥${(Number(form.quantity) * Number(form.unitPrice)).toFixed(2)}`)

async function loadRecords() {
    loading.value = true
    try {
        const result = await fetchConsumableInbounds({ current: 1, pageSize: 20 })
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
    userMap.value = optionsToMap(users)
}

function resetForm() {
    form.consumableId = undefined
    form.laboratoryId = undefined
    form.operatorUserId = undefined
    form.batchNo = ''
    form.inboundType = 1
    form.quantity = 1
    form.unitPrice = 0
    form.supplierName = ''
    form.inboundDate = ''
    form.productionDate = ''
    form.expiryDate = ''
    form.remarks = ''
}

async function submitForm() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    saving.value = true
    try {
        await createConsumableInbound(form)
        ElMessage.success('入库记录已创建')
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
