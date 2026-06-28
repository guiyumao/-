<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">危化品申领协同</h2>
                <p class="page-subtitle">危化品入库、领用、归还和废液处理统一登记，按批次校验库存并记录项目、见证人与申请来源。</p>
            </div>
            <el-space>
                <el-button @click="loadAll">刷新</el-button>
                <el-button v-if="authStore.hasPermission('hazardous_usage:edit')" type="primary" @click="openCreateDialog">新增记录</el-button>
            </el-space>
        </div>

        <div class="top-flow">
            <div v-for="step in workflowSteps" :key="step.title" class="flow-card">
                <span class="flow-index">{{ step.index }}</span>
                <div>
                    <strong>{{ step.title }}</strong>
                    <p>{{ step.desc }}</p>
                </div>
            </div>
        </div>

        <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">业务记录数</div>
                    <div class="metric-value">{{ records.length }}</div>
                    <div class="metric-note">申请、审批、操作与见证信息已串联</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">领用/处置数量</div>
                    <div class="metric-value">{{ issueQuantity }}</div>
                    <div class="metric-note">来自真实危化记录汇总</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">归还数量</div>
                    <div class="metric-value">{{ returnQuantity }}</div>
                    <div class="metric-note">用于闭环核对与库存回收</div>
                </el-card>
            </el-col>
        </el-row>

        <div class="sample-showcase">
            <div class="sample-section-head">
                <div>
                    <h3 class="section-title">教师与学生申请样例</h3>
                    <p class="section-desc">这里直接展示真实危化品申请链路，教师申请和学生申请都保留在同一条业务视图里。</p>
                </div>
                <span class="sample-tip">来源：危化品使用记录 + 用户账号 + 批次库存</span>
            </div>
            <div class="sample-grid">
                <div class="sample-card teacher" v-if="teacherSample">
                    <div class="sample-badge">教师申请样例</div>
                    <div class="sample-title">{{ userName(teacherSample.applicantUserId) }}申请 {{ materialName(teacherSample.hazardousMaterialId) }}</div>
                    <p class="sample-line">动作：{{ actionTypeText(teacherSample.actionType) }}</p>
                    <p class="sample-line">用途：{{ teacherSample.purpose }}</p>
                    <p class="sample-line">审批：{{ userName(teacherSample.approverUserId) }}</p>
                    <p class="sample-line">操作：{{ userName(teacherSample.operatorUserId) }}</p>
                    <p class="sample-line">见证：{{ teacherSample.witnessName || '-' }}</p>
                </div>
                <div class="sample-card student" v-if="studentSample">
                    <div class="sample-badge">学生申请样例</div>
                    <div class="sample-title">{{ userName(studentSample.applicantUserId) }}申请 {{ materialName(studentSample.hazardousMaterialId) }}</div>
                    <p class="sample-line">动作：{{ actionTypeText(studentSample.actionType) }}</p>
                    <p class="sample-line">用途：{{ studentSample.purpose }}</p>
                    <p class="sample-line">审批：{{ userName(studentSample.approverUserId) }}</p>
                    <p class="sample-line">操作：{{ userName(studentSample.operatorUserId) }}</p>
                    <p class="sample-line">见证：{{ studentSample.witnessName || '-' }}</p>
                </div>
            </div>
        </div>

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
                <el-table-column label="来源信息" min-width="240">
                    <template #default="{ row }">
                        <div class="data-stack">
                            <strong>申请人：{{ userMap[String(row.applicantUserId)] || row.applicantUserId || '-' }}</strong>
                            <span>审批人：{{ userMap[String(row.approverUserId)] || row.approverUserId || '-' }}</span>
                            <span>操作人：{{ userMap[String(row.operatorUserId)] || row.operatorUserId || '-' }}</span>
                            <span>见证人：{{ row.witnessName || '-' }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="remarks" label="来源说明" min-width="180" show-overflow-tooltip />
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" class="dark-dialog" title="新增危化品业务记录" width="840px">
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
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
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
                    <div v-if="form.actionType === 1" style="color: #92a0b2; font-size: 12px; margin-top: 6px">
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
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="审批人">
                    <el-select v-model="form.approverUserId" filterable placeholder="请选择审批人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="操作人">
                    <el-select v-model="form.operatorUserId" filterable placeholder="请选择操作人" style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
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
                <el-form-item label="来源说明">
                    <el-input v-model="form.remarks" type="textarea" :rows="2" maxlength="255" placeholder="例如：教师课题申请、学生实验消耗、废液处理记录" show-word-limit />
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
const userMap = ref<Record<string, string>>({})
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
const teacherSample = computed(() => records.value.find((item) => isTeacherApplicant(item.applicantUserId)))
const studentSample = computed(() => records.value.find((item) => isStudentApplicant(item.applicantUserId)))
const workflowSteps = [
    { index: '01', title: '申请登记', desc: '教师或学生填写用途、项目和批次来源。' },
    { index: '02', title: '审批领用', desc: '审批人确认后，操作人完成出库或废液处理。' },
    { index: '03', title: '见证留痕', desc: '每条记录都保留申请人、见证人与来源说明。' },
]

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

function materialName(materialId?: number) {
    if (!materialId) return '-'
    return materialMap.value[String(materialId)] || String(materialId)
}

function userName(userId?: number) {
    if (!userId) return '-'
    return userMap.value[String(userId)] || String(userId)
}

function userOption(userId?: number) {
    return userOptions.value.find((item) => item.id === userId)
}

function isTeacherApplicant(userId?: number) {
    const option = userOption(userId)
    if (!option) return false
    return option.subLabel?.startsWith('teacher_') || option.label.endsWith('老师')
}

function isStudentApplicant(userId?: number) {
    const option = userOption(userId)
    if (!option) return false
    return option.subLabel?.startsWith('student_') || option.label.endsWith('同学')
}

function openCreateDialog() {
    resetForm()
    dialogVisible.value = true
}

async function loadAll() {
    await Promise.all([loadOptions(), loadRecords()])
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
    userMap.value = optionsToMap(users)
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
    await loadAll()
})
</script>

<style scoped>
.top-flow {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 20px;
}

.flow-card,
.metric-card,
.sample-showcase,
.dark-dialog :deep(.el-dialog) {
    border: 1px solid rgba(116, 145, 155, 0.16);
    background: linear-gradient(145deg, rgba(13, 18, 24, 0.99), rgba(25, 31, 39, 0.99));
    box-shadow: 0 18px 40px rgba(0, 0, 0, 0.32);
}

.flow-card {
    display: flex;
    gap: 14px;
    min-height: 100px;
    padding: 18px;
    border-radius: 20px;
}

.flow-index {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 999px;
    background: rgba(93, 173, 255, 0.15);
    color: #94d3ff;
    font-weight: 800;
}

.flow-card strong,
.section-title {
    color: #f5f7fa;
    font-weight: 800;
}

.flow-card p,
.page-subtitle,
.section-desc,
.sample-line {
    color: #a8b3c2;
    line-height: 1.68;
}

.stats-row {
    margin-bottom: 20px;
}

.metric-card {
    border-radius: 20px;
}

.metric-label {
    color: #92a0b2;
    font-size: 14px;
    font-weight: 700;
}

.metric-value {
    margin-top: 10px;
    color: #f4f7fb;
    font-family: var(--font-display);
    font-size: 34px;
    font-weight: 800;
}

.metric-note {
    margin-top: 8px;
    color: #92a0b2;
    font-size: 13px;
}

.sample-showcase {
    margin-bottom: 20px;
    padding: 22px;
    border-radius: 24px;
}

.sample-section-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;
}

.sample-tip {
    color: #7f8da0;
    font-size: 12px;
    line-height: 1.6;
}

.sample-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
}

.sample-card {
    padding: 18px;
    border-radius: 20px;
    border: 1px solid rgba(116, 145, 155, 0.16);
    background: rgba(255, 255, 255, 0.04);
}

.sample-card.teacher {
    box-shadow: inset 0 0 0 1px rgba(255, 184, 77, 0.12);
}

.sample-card.student {
    box-shadow: inset 0 0 0 1px rgba(88, 164, 255, 0.12);
}

.sample-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 10px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.08);
    color: #dbe6f5;
    font-size: 12px;
    font-weight: 700;
}

.sample-title {
    margin-top: 14px;
    color: #f5f7fa;
    font-size: 20px;
    font-weight: 800;
    line-height: 1.5;
}

.dark-dialog :deep(.el-dialog) {
    border-radius: 22px;
    border: 1px solid rgba(116, 145, 155, 0.18);
    background: rgba(13, 17, 23, 0.99);
    box-shadow: 0 28px 60px rgba(0, 0, 0, 0.45);
}

.dark-dialog :deep(.el-dialog__title) {
    color: #f5f7fa;
}

.dark-dialog :deep(.el-dialog__body) {
    color: #dbe6f5;
}

.dark-dialog :deep(.el-input__wrapper),
.dark-dialog :deep(.el-textarea__inner),
.dark-dialog :deep(.el-select__wrapper) {
    background: rgba(255, 255, 255, 0.05);
    box-shadow: 0 0 0 1px rgba(116, 145, 155, 0.14) inset;
}

.dark-dialog :deep(.el-form-item__label),
.dark-dialog :deep(.el-radio__label) {
    color: #dbe6f5;
}

@media (max-width: 1100px) {
    .top-flow,
    .sample-grid {
        grid-template-columns: 1fr;
    }

    .sample-section-head {
        flex-direction: column;
        align-items: stretch;
    }
}
</style>
