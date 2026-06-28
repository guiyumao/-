<template>
  <div>
    <div class="page-header">
      <div>
        <h2 class="page-title">耗材申领协同</h2>
        <p class="page-subtitle">把申请人、审批人、操作人和库存批次放在同一条链路里，避免出库只剩一条孤立台账。</p>
      </div>
      <el-space>
        <el-button @click="loadAll">刷新</el-button>
        <el-button v-if="authStore.hasPermission('consumable_outbound:edit')" type="primary" @click="openCreateDialog">新增出库</el-button>
      </el-space>
    </div>

    <div class="flow-overview">
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
          <div class="metric-label">出库记录数</div>
          <div class="metric-value">{{ records.length }}</div>
          <div class="metric-note">记录已串联申请人、审批人和操作人</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="card-panel metric-card" shadow="never">
          <div class="metric-label">出库数量</div>
          <div class="metric-value">{{ totalQuantity }}</div>
          <div class="metric-note">按申领、实验使用、调拨等类型汇总</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="card-panel metric-card" shadow="never">
          <div class="metric-label">出库金额</div>
          <div class="metric-value">¥{{ totalAmount }}</div>
          <div class="metric-note">来源于真实出库记录汇总</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="sample-showcase">
      <div class="sample-section-head">
        <div>
          <h3 class="section-title">教师与学生申请样例</h3>
          <p class="section-desc">样例直接来自当前出库记录，和批次、审批、操作人一起展示，不额外硬编码。</p>
        </div>
        <span class="sample-tip">来源：耗材出库记录 + 用户账号 + 库存批次</span>
      </div>
      <div class="sample-grid">
        <div v-if="teacherSample" class="sample-card teacher">
          <div class="sample-badge">教师申请样例</div>
          <div class="sample-title">{{ userName(teacherSample.applicantUserId) }}申领 {{ consumableName(teacherSample.consumableId) }}</div>
          <p class="sample-line">批次：{{ teacherSample.batchNo }}</p>
          <p class="sample-line">用途：{{ teacherSample.purpose }}</p>
          <p class="sample-line">审批：{{ userName(teacherSample.approverUserId) }}</p>
          <p class="sample-line">操作：{{ userName(teacherSample.operatorUserId) }}</p>
          <p class="sample-line">来源：{{ outboundSourceText(teacherSample) }}</p>
        </div>
        <div v-if="studentSample" class="sample-card student">
          <div class="sample-badge">学生申请样例</div>
          <div class="sample-title">{{ userName(studentSample.applicantUserId) }}申领 {{ consumableName(studentSample.consumableId) }}</div>
          <p class="sample-line">批次：{{ studentSample.batchNo }}</p>
          <p class="sample-line">用途：{{ studentSample.purpose }}</p>
          <p class="sample-line">审批：{{ userName(studentSample.approverUserId) }}</p>
          <p class="sample-line">操作：{{ userName(studentSample.operatorUserId) }}</p>
          <p class="sample-line">来源：{{ outboundSourceText(studentSample) }}</p>
        </div>
      </div>
    </div>

    <el-card class="card-panel" shadow="never">
      <div class="toolbar">
        <div>
          <h3 class="section-title">耗材申领记录</h3>
          <p class="section-desc">每条记录都保留申请人、审批人、操作人和来源说明，便于跨角色追溯。</p>
        </div>
      </div>

      <div class="record-grid">
        <el-card v-for="row in records" :key="row.id" class="business-card" shadow="never">
          <div class="business-card-head">
            <div>
              <div class="record-title">
                {{ consumableName(row.consumableId) }}
                <el-tag effect="light" type="warning">{{ outboundTypeText(row.outboundType) }}</el-tag>
              </div>
              <p class="record-subtitle">批号：{{ row.batchNo }} · 时间：{{ row.outboundDate }}</p>
            </div>
            <div class="next-box">
              <span class="mini-label">金额</span>
              <strong>¥{{ Number(row.totalAmount ?? 0).toFixed(2) }}</strong>
            </div>
          </div>

          <div class="chain-row">
            <div class="chain-node">
              <span class="mini-label">申请人</span>
              <strong>{{ userName(row.applicantUserId) }}</strong>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-node">
              <span class="mini-label">审批人</span>
              <strong>{{ userName(row.approverUserId) }}</strong>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-node">
              <span class="mini-label">操作人</span>
              <strong>{{ userName(row.operatorUserId) }}</strong>
            </div>
          </div>

          <div class="detail-grid">
            <div class="detail-card">
              <div class="detail-title">出库信息</div>
              <p>数量：{{ row.quantity }}</p>
              <p>单价：{{ row.unitPrice }}</p>
              <p>用途：{{ row.purpose }}</p>
            </div>
            <div class="detail-card">
              <div class="detail-title">来源说明</div>
              <p>{{ outboundSourceText(row) }}</p>
              <p v-if="row.remarks">备注：{{ row.remarks }}</p>
            </div>
          </div>

          <div class="action-row">
            <span class="action-hint">来源已串联到申请、审批和库存批次，删除前请先确认这条记录不再需要追溯。</span>
            <el-space v-if="authStore.hasPermission('consumable_outbound:edit')" wrap>
              <el-button size="small" type="danger" plain @click="deleteRecord(row)">删除</el-button>
            </el-space>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" class="dark-dialog" title="新增耗材出库" width="820px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="实验室" prop="laboratoryId">
          <el-select v-model="form.laboratoryId" placeholder="请选择实验室" style="width: 100%" @change="handleInventoryDependencyChange">
            <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="耗材" prop="consumableId">
          <el-select v-model="form.consumableId" filterable placeholder="请选择耗材" style="width: 100%" @change="handleInventoryDependencyChange">
            <el-option v-for="item in consumableOptions" :key="item.id" :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存批次" prop="batchNo">
          <el-select v-model="form.batchNo" placeholder="请选择可用批次" style="width: 100%" @change="handleBatchChange">
            <el-option v-for="item in inventoryOptions" :key="item.id" :label="`${item.batchNo} / 可用 ${item.availableQuantity}`" :value="item.batchNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人" prop="applicantUserId">
          <el-select v-model="form.applicantUserId" filterable placeholder="请选择申请人" style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人" prop="approverUserId">
          <el-select v-model="form.approverUserId" filterable placeholder="请选择审批人" style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-select v-model="form.operatorUserId" filterable placeholder="请选择操作人" style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label" :value="item.id" />
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
        <el-form-item label="来源说明">
          <el-input v-model="form.remarks" type="textarea" :rows="2" maxlength="255" placeholder="例如：教师教学实验、学生课题领用、调拨补货" show-word-limit />
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createConsumableOutbound,
  deleteConsumableOutbound,
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

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const records = ref<ConsumableOutboundRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const consumableOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const inventoryOptions = ref<InventoryOption[]>([])
const consumableMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const formRef = ref<FormInstance>()

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
const teacherSample = computed(() => records.value.find((item) => isTeacherApplicant(item.applicantUserId)))
const studentSample = computed(() => records.value.find((item) => isStudentApplicant(item.applicantUserId)))

const workflowSteps = [
  { index: '01', title: '申请登记', desc: '教师或学生填写用途，系统绑定库存批次。' },
  { index: '02', title: '审批出库', desc: '审批人确认后，由操作人完成出库。' },
  { index: '03', title: '来源追溯', desc: '记录与后续库存、报表、提醒统一关联。' },
]

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

async function loadAll() {
  await Promise.all([loadOptions(), loadRecords()])
}

async function loadOptions() {
  const [labs, consumables, users] = await Promise.all([fetchLaboratoryOptions(), fetchConsumableOptions(), fetchUserOptions()])
  laboratoryOptions.value = labs
  consumableOptions.value = consumables
  userOptions.value = users
  consumableMap.value = optionsToMap(consumables)
  userMap.value = optionsToMap(users)
}

async function handleInventoryDependencyChange() {
  form.batchNo = ''
  form.unitPrice = 0
  inventoryOptions.value = []
  if (!form.laboratoryId || !form.consumableId) return
  inventoryOptions.value = await fetchInventoryOptions(form.laboratoryId, 2, form.consumableId)
}

function handleBatchChange() {
  form.unitPrice = selectedInventory.value?.unitPrice ?? 0
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function consumableName(consumableId?: number) {
  if (!consumableId) return '-'
  return consumableMap.value[String(consumableId)] || String(consumableId)
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

function outboundTypeText(type?: number) {
  if (type === 1) return '教学领用'
  if (type === 2) return '科研领用'
  if (type === 3) return '调拨'
  return '损耗'
}

function outboundSourceText(row: ConsumableOutboundRecord) {
  return `申请人 ${userName(row.applicantUserId)}，审批人 ${userName(row.approverUserId)}，操作人 ${userName(row.operatorUserId)}，批次 ${row.batchNo}。`
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
  if (!valid) return
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

async function deleteRecord(row: ConsumableOutboundRecord) {
  const confirmed = await ElMessageBox.confirm(
    `确认删除 ${consumableName(row.consumableId)} 的出库记录吗？删除后会影响库存和跨角色追溯。`,
    '确认删除出库记录',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' },
  ).catch(() => false)
  if (!confirmed) return
  await deleteConsumableOutbound(row.id)
  ElMessage.success('出库记录已删除')
  await loadAll()
}

onMounted(async () => {
  await loadAll()
})
</script>

<style scoped>
.flow-overview,
.sample-grid,
.detail-grid,
.record-grid {
  display: grid;
  gap: 16px;
}

.flow-overview {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 20px;
}

.sample-grid,
.detail-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.flow-card,
.metric-card,
.sample-showcase,
.business-card,
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
.section-title,
.record-title {
  color: #f5f7fa;
  font-weight: 800;
}

.flow-card p,
.page-subtitle,
.section-desc,
.record-subtitle,
.detail-card p,
.sample-line,
.mini-label {
  color: #a8b3c2;
  line-height: 1.68;
}

.metric-card,
.sample-showcase,
.business-card {
  border-radius: 20px;
}

.metric-label,
.mini-label,
.detail-title {
  color: #92a0b2;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.metric-value {
  margin-top: 10px;
  color: #f4f7fb;
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 800;
}

.metric-note,
.sample-tip {
  color: #7f8da0;
  font-size: 13px;
}

.sample-showcase {
  margin-bottom: 20px;
  padding: 22px;
}

.sample-section-head,
.toolbar,
.business-card-head,
.action-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.sample-section-head {
  align-items: flex-start;
  margin-bottom: 16px;
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

.toolbar {
  align-items: center;
  margin-bottom: 18px;
}

.business-card {
  padding: 18px;
  border-radius: 24px;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
}

.next-box,
.chain-node,
.detail-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(116, 145, 155, 0.12);
}

.next-box strong,
.chain-node strong {
  display: block;
  margin-top: 8px;
  color: #f5f7fa;
}

.chain-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  align-items: center;
  gap: 12px;
  margin: 18px 0;
}

.chain-arrow {
  justify-self: center;
  color: #76baff;
  font-size: 22px;
  font-weight: 800;
}

.detail-title {
  margin-bottom: 10px;
}

.action-row {
  align-items: center;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed rgba(116, 145, 155, 0.18);
}

.action-hint {
  color: #a8b3c2;
  line-height: 1.6;
}

.dark-dialog :deep(.el-dialog__header) {
  padding: 22px 24px 12px;
  border-bottom: 1px solid rgba(116, 145, 155, 0.12);
}

.dark-dialog :deep(.el-dialog__title) {
  color: #f5f7fa;
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
}

.dark-dialog :deep(.el-dialog__body) {
  padding: 24px;
  color: #dbe6f5;
}

.dark-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 24px;
  border-top: 1px solid rgba(116, 145, 155, 0.12);
}

.dark-dialog :deep(.el-form-item__label),
.dark-dialog :deep(.el-radio__label),
.dark-dialog :deep(.el-input__wrapper),
.dark-dialog :deep(.el-textarea__inner),
.dark-dialog :deep(.el-select__wrapper) {
  color: #dbe6f5;
}

.dark-dialog :deep(.el-input__wrapper),
.dark-dialog :deep(.el-textarea__inner),
.dark-dialog :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 1px rgba(116, 145, 155, 0.14) inset;
}

@media (max-width: 1100px) {
  .flow-overview,
  .sample-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .chain-row {
    grid-template-columns: 1fr;
  }

  .chain-arrow {
    transform: rotate(90deg);
  }

  .business-card-head,
  .action-row,
  .toolbar,
  .sample-section-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
