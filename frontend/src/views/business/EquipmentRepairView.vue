<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">设备维修协同</h2>
                <p class="page-subtitle">报修不再是孤立记录，而是承接借用归还异常、分派维修人员、回传设备状态的协同链路。</p>
            </div>
            <el-space>
                <el-button @click="loadAll">刷新</el-button>
                <el-button v-if="authStore.hasPermission('equipment_repair:edit')" type="primary" @click="openCreate">
                    新增维修
                </el-button>
            </el-space>
        </div>

        <div class="repair-overview">
            <div v-for="step in workflowSteps" :key="step.title" class="overview-card">
                <span class="overview-index">{{ step.index }}</span>
                <div>
                    <strong>{{ step.title }}</strong>
                    <p>{{ step.desc }}</p>
                </div>
            </div>
        </div>

        <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">待处理报修</div>
                    <div class="metric-value">{{ pendingCount }}</div>
                    <div class="metric-note">等待指派维修角色</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">维修中</div>
                    <div class="metric-value warning">{{ processingCount }}</div>
                    <div class="metric-note">需继续跟踪处理进度</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">关联借用异常</div>
                    <div class="metric-value">{{ borrowLinkedCount }}</div>
                    <div class="metric-note">来自借用归还检查或现场故障反馈</div>
                </el-card>
            </el-col>
        </el-row>

        <el-card class="card-panel" shadow="never">
            <div class="toolbar">
                <div>
                    <h3 class="section-title">维修协同记录</h3>
                    <p class="section-desc">每条记录都展示报修人、维修人、来源说明以及与借用和校准的关联状态。</p>
                </div>
            </div>

            <div class="record-grid">
                <el-card v-for="record in records" :key="record.id" class="repair-card" shadow="never">
                    <div class="repair-head">
                        <div>
                            <div class="record-title">
                                {{ equipmentName(record.equipmentId) }}
                                <el-tag :type="repairStatusType(record.repairStatus)" effect="light">
                                    {{ repairStatusText(record.repairStatus) }}
                                </el-tag>
                            </div>
                            <p class="record-subtitle">{{ laboratoryName(record.laboratoryId) }} · 故障描述：{{ record.faultDescription }}</p>
                        </div>
                        <div class="next-box">
                            <span class="mini-label">下一步</span>
                            <strong>{{ nextActorText(record) }}</strong>
                        </div>
                    </div>

                    <div class="chain-row">
                        <div class="chain-node">
                            <span class="mini-label">报修人</span>
                            <strong>{{ userName(record.reporterUserId) }}</strong>
                        </div>
                        <div class="chain-arrow">→</div>
                        <div class="chain-node">
                            <span class="mini-label">维修人</span>
                            <strong>{{ userName(record.repairUserId) }}</strong>
                        </div>
                        <div class="chain-arrow">→</div>
                        <div class="chain-node">
                            <span class="mini-label">回传对象</span>
                            <strong>{{ repairFeedbackTarget(record) }}</strong>
                        </div>
                    </div>

                    <div class="detail-grid">
                        <div class="detail-card">
                            <div class="detail-title">时间与费用</div>
                            <p>报修时间：{{ formatDateTime(record.reportTime) }}</p>
                            <p>开始维修：{{ formatDateTime(record.repairStartTime) }}</p>
                            <p>完成维修：{{ formatDateTime(record.repairEndTime) }}</p>
                            <p>维修费用：{{ repairCostText(record.repairCost) }}</p>
                        </div>
                        <div class="detail-card">
                            <div class="detail-title">来源信息</div>
                            <p>{{ repairSourceText(record) }}</p>
                            <p v-if="record.repairResult">维修结果：{{ record.repairResult }}</p>
                            <p v-if="record.remarks">备注来源：{{ record.remarks }}</p>
                        </div>
                    </div>

                    <div class="link-grid">
                        <div class="link-card">
                            <div class="detail-title">借用联动</div>
                            <template v-if="linkedBorrow(record)">
                                <p>申请人：{{ userName(linkedBorrow(record)?.borrowerUserId) }}</p>
                                <p>审批人：{{ userName(linkedBorrow(record)?.approverUserId) }}</p>
                                <p>归还结果：{{ linkedBorrow(record)?.returnCondition || '待登记' }}</p>
                            </template>
                            <p v-else>当前未匹配到借用记录，说明该报修可能来自日常巡检或现场使用反馈。</p>
                        </div>
                        <div class="link-card">
                            <div class="detail-title">校准联动</div>
                            <template v-if="linkedCalibration(record)">
                                <p>校准人员：{{ userName(linkedCalibration(record)?.calibrationUserId) }}</p>
                                <p>校准状态：{{ calibrationStatusText(linkedCalibration(record)?.calibrationStatus) }}</p>
                                <p>有效期：{{ formatDateTime(linkedCalibration(record)?.validUntil) }}</p>
                            </template>
                            <p v-else>维修完成后可继续安排校准，确保设备恢复可用前有完整闭环。</p>
                        </div>
                    </div>

                    <div class="footer-row">
                        <span class="action-hint">{{ actionHint(record) }}</span>
                        <el-button
                            v-if="authStore.hasPermission('equipment_repair:edit')"
                            size="small"
                            type="primary"
                            plain
                            @click="openUpdate(record)"
                        >
                            更新状态
                        </el-button>
                    </div>
                </el-card>
            </div>
        </el-card>

        <el-dialog v-model="createVisible" class="repair-dialog" title="新增维修记录" width="760px">
            <p class="dialog-tip">请明确报修来源，方便后续判断是借用归还异常、日常巡检问题，还是校准前检查发现的故障。</p>
            <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="140px">
                <el-form-item label="实验室" prop="laboratoryId">
                    <el-select v-model="createForm.laboratoryId" style="width: 100%">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="设备" prop="equipmentId">
                    <el-select v-model="createForm.equipmentId" filterable style="width: 100%">
                        <el-option
                            v-for="item in equipmentOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="报修人" prop="reporterUserId">
                    <el-select v-model="createForm.reporterUserId" filterable style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="故障描述" prop="faultDescription">
                    <el-input v-model="createForm.faultDescription" type="textarea" :rows="3" maxlength="500" show-word-limit />
                </el-form-item>
                <el-form-item label="报修时间" prop="reportTime">
                    <el-date-picker v-model="createForm.reportTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="来源说明">
                    <el-input
                        v-model="createForm.remarks"
                        type="textarea"
                        :rows="2"
                        maxlength="255"
                        placeholder="例如：来源于借用归还检查、现场点检单或教师报修电话"
                        show-word-limit
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="createVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
                </el-space>
            </template>
        </el-dialog>

        <el-dialog v-model="updateVisible" class="repair-dialog" title="更新维修状态" width="760px">
            <p class="dialog-tip">维修状态会直接影响设备可用性，也决定是否需要继续校准或通知原申请人。</p>
            <el-form ref="updateFormRef" :model="updateForm" :rules="updateRules" label-width="140px">
                <el-form-item label="维修人员" prop="repairUserId">
                    <el-select v-model="updateForm.repairUserId" filterable style="width: 100%">
                        <el-option
                            v-for="item in userOptions"
                            :key="item.id"
                            :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
                            :value="item.id"
                        />
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
                <el-form-item label="来源说明">
                    <el-input
                        v-model="updateForm.remarks"
                        type="textarea"
                        :rows="2"
                        maxlength="255"
                        placeholder="例如：来源于维修工单、检测报告或现场复核记录"
                        show-word-limit
                    />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
    createEquipmentRepair,
    fetchEquipmentBorrows,
    fetchEquipmentCalibrations,
    fetchEquipmentOptions,
    fetchEquipmentRepairs,
    fetchLaboratoryOptions,
    fetchUserOptions,
    optionsToMap,
    type EquipmentBorrowRecord,
    type EquipmentCalibrationRecord,
    type EquipmentRepairRecord,
    type SelectOption,
    updateEquipmentRepairStatus,
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'
import { formatDateTime } from './EquipmentBorrowBusinessHelpers'

const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const updateVisible = ref(false)
const currentId = ref<number>()
const records = ref<EquipmentRepairRecord[]>([])
const borrowRecords = ref<EquipmentBorrowRecord[]>([])
const calibrationRecords = ref<EquipmentCalibrationRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const laboratoryMap = ref<Record<string, string>>({})
const equipmentMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const updateFormRef = ref<FormInstance>()

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

const workflowSteps = [
    { index: '01', title: '报修发起', desc: '报修人登记故障、时间和来源，系统锁定设备进入维修链。' },
    { index: '02', title: '角色接力', desc: '实验室或管理员分派维修人，维修人回填进度、费用和处理结果。' },
    { index: '03', title: '状态回传', desc: '修复结果返回设备台账，并决定是否继续安排校准或报废。' },
]

const pendingCount = computed(() => records.value.filter((item) => item.repairStatus === 1).length)
const processingCount = computed(() => records.value.filter((item) => item.repairStatus === 2).length)
const borrowLinkedCount = computed(() => records.value.filter((item) => linkedBorrow(item)).length)

function repairStatusText(status?: number) {
    if (status === 2) return '维修中'
    if (status === 3) return '已修复'
    if (status === 4) return '无法修复'
    return '待处理'
}

function repairStatusType(status?: number) {
    if (status === 2) return 'warning'
    if (status === 3) return 'success'
    if (status === 4) return 'danger'
    return 'info'
}

function calibrationStatusText(status?: number) {
    if (status === 2) return '已确认'
    if (status === 1) return '待确认'
    return '待登记'
}

function equipmentName(id?: number) {
    if (!id) return '-'
    return equipmentMap.value[String(id)] || String(id)
}

function laboratoryName(id?: number) {
    if (!id) return '-'
    return laboratoryMap.value[String(id)] || String(id)
}

function userName(id?: number) {
    if (!id) return '待分配'
    return userMap.value[String(id)] || String(id)
}

function repairCostText(value?: number | string | null) {
    if (value === undefined || value === null || value === '') {
        return '-'
    }
    return `¥${value}`
}

function linkedBorrow(record: EquipmentRepairRecord) {
    return borrowRecords.value.find((item) => item.equipmentId === record.equipmentId)
}

function linkedCalibration(record: EquipmentRepairRecord) {
    return calibrationRecords.value.find((item) => item.equipmentId === record.equipmentId)
}

function repairSourceText(record: EquipmentRepairRecord) {
    return `报修人 ${userName(record.reporterUserId)} 提交，故障描述为“${record.faultDescription}”。`
}

function nextActorText(record: EquipmentRepairRecord) {
    if (record.repairStatus === 1) {
        return '等待分派维修人员'
    }
    if (record.repairStatus === 2) {
        return `${userName(record.repairUserId)} 继续处理`
    }
    if (record.repairStatus === 3 && linkedCalibration(record)) {
        return '关注校准确认与恢复可用'
    }
    if (record.repairStatus === 3) {
        return '设备可恢复使用，必要时安排校准'
    }
    if (record.repairStatus === 4) {
        return '提交报废或替代方案评估'
    }
    return '等待处理'
}

function repairFeedbackTarget(record: EquipmentRepairRecord) {
    const borrow = linkedBorrow(record)
    if (borrow) {
        return `${userName(borrow.borrowerUserId)} 与实验室管理员`
    }
    return '设备台账与实验室负责人'
}

function actionHint(record: EquipmentRepairRecord) {
    if (record.repairStatus === 1) {
        return '当前尚未开始维修，建议尽快分派维修角色。'
    }
    if (record.repairStatus === 2) {
        return '维修处理中，需持续回填开始时间、过程说明和费用。'
    }
    if (record.repairStatus === 3) {
        return '维修完成后，可继续安排校准或恢复借用。'
    }
    if (record.repairStatus === 4) {
        return '维修失败，建议同步实验室负责人评估报废或替换。'
    }
    return '等待进一步处理。'
}

function openCreate() {
    createVisible.value = true
}

function openUpdate(row: EquipmentRepairRecord) {
    currentId.value = row.id
    updateForm.repairUserId = row.repairUserId
    updateForm.repairStatus = row.repairStatus || 2
    updateForm.repairStartTime = row.repairStartTime ?? ''
    updateForm.repairEndTime = row.repairEndTime ?? ''
    updateForm.repairCost = Number(row.repairCost ?? 0)
    updateForm.repairResult = row.repairResult ?? ''
    updateForm.remarks = row.remarks ?? ''
    updateVisible.value = true
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

async function loadAll() {
    loading.value = true
    try {
        const [repairResult, borrowResult, calibrationResult] = await Promise.all([
            fetchEquipmentRepairs({ current: 1, pageSize: 50 }),
            fetchEquipmentBorrows({ current: 1, pageSize: 50 }),
            fetchEquipmentCalibrations({ current: 1, pageSize: 50 }),
        ])
        records.value = repairResult.data.records
        borrowRecords.value = borrowResult.data.records
        calibrationRecords.value = calibrationResult.data.records
    } finally {
        loading.value = false
    }
}

async function submitCreate() {
    const valid = await createFormRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    try {
        await createEquipmentRepair(createForm)
        ElMessage.success('维修记录已创建')
        createVisible.value = false
        await loadAll()
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
        await loadAll()
    } finally {
        saving.value = false
    }
}

onMounted(async () => {
    await Promise.all([loadOptions(), loadAll()])
})
</script>

<style scoped>
.repair-overview {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 20px;
}

.overview-card {
    display: flex;
    gap: 14px;
    min-height: 100px;
    padding: 18px;
    border-radius: 20px;
    border: 1px solid rgba(30, 52, 60, 0.12);
    background:
        linear-gradient(155deg, rgba(255, 252, 244, 0.98), rgba(246, 251, 255, 0.98));
    box-shadow: 0 16px 34px rgba(23, 45, 52, 0.08);
}

.overview-index {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 999px;
    background: rgba(188, 114, 32, 0.14);
    color: #93510d;
    font-weight: 800;
}

.overview-card strong,
.section-title,
.record-title {
    color: var(--text-main);
    font-weight: 800;
}

.overview-card p,
.page-subtitle,
.section-desc,
.record-subtitle,
.detail-card p,
.link-card p,
.dialog-tip {
    margin: 6px 0 0;
    color: var(--text-secondary);
    line-height: 1.68;
}

.stats-row {
    margin-bottom: 20px;
}

.metric-card {
    border-radius: 20px;
    background:
        radial-gradient(circle at top right, rgba(255, 176, 107, 0.18), transparent 40%),
        linear-gradient(160deg, rgba(255, 255, 255, 0.98), rgba(250, 247, 241, 0.98));
}

.metric-label {
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 700;
}

.metric-value {
    margin-top: 10px;
    color: var(--text-main);
    font-family: var(--font-display);
    font-size: 34px;
    font-weight: 800;
}

.metric-value.warning {
    color: #b96b18;
}

.metric-note {
    margin-top: 8px;
    color: var(--text-secondary);
    font-size: 13px;
}

.toolbar {
    margin-bottom: 18px;
}

.record-grid {
    display: grid;
    gap: 18px;
}

.repair-card {
    border-radius: 24px;
    border: 1px solid rgba(30, 52, 60, 0.1);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(249, 245, 239, 0.98));
    box-shadow: 0 18px 40px rgba(23, 45, 52, 0.08);
}

.repair-head,
.footer-row {
    display: flex;
    justify-content: space-between;
    gap: 18px;
}

.record-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
}

.next-box {
    min-width: 180px;
    padding: 14px 16px;
    border-radius: 18px;
    background: rgba(188, 114, 32, 0.08);
    border: 1px solid rgba(188, 114, 32, 0.14);
}

.mini-label,
.detail-title {
    display: block;
    color: var(--text-secondary);
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.04em;
}

.next-box strong,
.chain-node strong {
    display: block;
    margin-top: 8px;
    color: var(--text-main);
}

.chain-row {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    align-items: center;
    gap: 12px;
    margin: 18px 0;
}

.chain-node,
.detail-card,
.link-card {
    padding: 16px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.84);
    border: 1px solid rgba(30, 52, 60, 0.09);
}

.chain-arrow {
    justify-self: center;
    color: #93510d;
    font-size: 22px;
    font-weight: 800;
}

.detail-grid,
.link-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
}

.detail-title {
    margin-bottom: 10px;
}

.footer-row {
    align-items: center;
    margin-top: 18px;
    padding-top: 16px;
    border-top: 1px dashed rgba(30, 52, 60, 0.12);
}

.action-hint {
    color: var(--text-secondary);
    line-height: 1.6;
}

.dialog-tip {
    margin: 0 0 18px;
    padding: 12px 14px;
    border-radius: 14px;
    background: rgba(188, 114, 32, 0.08);
    border: 1px solid rgba(188, 114, 32, 0.14);
}

.repair-dialog :deep(.el-dialog) {
    background: rgba(255, 248, 240, 0.99);
    border: 1px solid rgba(30, 52, 60, 0.12);
    box-shadow: 0 28px 60px rgba(23, 45, 52, 0.2);
}

.repair-dialog :deep(.el-dialog__header) {
    padding: 22px 24px 12px;
    border-bottom: 1px solid rgba(30, 52, 60, 0.08);
}

.repair-dialog :deep(.el-dialog__title) {
    color: var(--text-main);
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
}

.repair-dialog :deep(.el-dialog__body) {
    padding: 24px;
}

.repair-dialog :deep(.el-dialog__footer) {
    padding: 12px 24px 24px;
    border-top: 1px solid rgba(30, 52, 60, 0.08);
}

@media (max-width: 1100px) {
    .repair-overview,
    .detail-grid,
    .link-grid {
        grid-template-columns: 1fr;
    }

    .chain-row {
        grid-template-columns: 1fr;
    }

    .chain-arrow {
        transform: rotate(90deg);
    }

    .repair-head,
    .footer-row {
        flex-direction: column;
        align-items: stretch;
    }

    .next-box {
        min-width: auto;
    }
}
</style>
