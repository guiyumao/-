<template>
  <div>
    <div class="page-header">
      <div>
        <h2 class="page-title">设备校准协同</h2>
        <p class="page-subtitle">把校准任务、确认角色、借用可用性和提醒到期信息串起来，避免校准只停留在证书记录层面。</p>
      </div>
      <el-space>
        <el-button @click="loadAll">刷新</el-button>
        <el-button v-if="authStore.hasPermission('equipment_calibration:edit')" type="primary" @click="openCreate">新增校准</el-button>
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
          <div class="metric-label">待确认校准</div>
          <div class="metric-value warning">{{ pendingCount }}</div>
          <div class="metric-note">需要校准角色完成确认</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="card-panel metric-card" shadow="never">
          <div class="metric-label">已关联借用</div>
          <div class="metric-value">{{ borrowLinkedCount }}</div>
          <div class="metric-note">设备校准直接影响借用可用性</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="card-panel metric-card" shadow="never">
          <div class="metric-label">提醒关注中</div>
          <div class="metric-value">{{ reminderLinkedCount }}</div>
          <div class="metric-note">到期设备会在提醒中心同步展示</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="sample-showcase">
      <div class="sample-section-head">
        <div>
          <h3 class="section-title">申请与校准联动样例</h3>
          <p class="section-desc">从真实借用记录反推校准影响，直接看到教师申请样例和学生申请样例如何进入校准、提醒与可用性判断。</p>
        </div>
        <span class="sample-tip">来源：设备借用记录、校准记录、维修记录共同计算</span>
      </div>
      <div class="sample-grid">
        <div v-if="teacherBorrowSample" class="sample-card teacher">
          <div class="sample-badge">教师申请样例</div>
          <div class="sample-title">{{ userName(teacherBorrowSample.borrowerUserId) }}申请 {{ equipmentName(teacherBorrowSample.equipmentId) }}</div>
          <p class="sample-line">借用用途：{{ teacherBorrowSample.purpose }}</p>
          <p class="sample-line">审批：{{ userName(teacherBorrowSample.approverUserId) }}</p>
          <p class="sample-line">关联校准：{{ linkedCalibrationSummary(teacherBorrowSample) }}</p>
          <p class="sample-line">联动判断：{{ linkedCalibrationAction(teacherBorrowSample) }}</p>
        </div>
        <div v-if="studentBorrowSample" class="sample-card student">
          <div class="sample-badge">学生申请样例</div>
          <div class="sample-title">{{ userName(studentBorrowSample.borrowerUserId) }}申请 {{ equipmentName(studentBorrowSample.equipmentId) }}</div>
          <p class="sample-line">借用用途：{{ studentBorrowSample.purpose }}</p>
          <p class="sample-line">审批：{{ userName(studentBorrowSample.approverUserId) }}</p>
          <p class="sample-line">关联校准：{{ linkedCalibrationSummary(studentBorrowSample) }}</p>
          <p class="sample-line">联动判断：{{ linkedCalibrationAction(studentBorrowSample) }}</p>
        </div>
      </div>
    </div>

    <el-card class="card-panel" shadow="never">
      <div class="toolbar">
        <div>
          <h3 class="section-title">校准协同记录</h3>
          <p class="section-desc">按设备展示发起、确认、借用影响与提醒关联，让校准真正成为跨角色闭环。</p>
        </div>
      </div>

      <div class="record-grid">
        <el-card v-for="record in records" :key="record.id" class="calibration-card" shadow="never">
          <div class="card-head">
            <div>
              <div class="record-title">
                {{ equipmentName(record.equipmentId) }}
                <el-tag :type="calibrationStatusType(record.calibrationStatus)" effect="light">{{ calibrationStatusText(record.calibrationStatus) }}</el-tag>
              </div>
              <p class="record-subtitle">{{ laboratoryName(record.laboratoryId) }} · 证书编号：{{ record.certificateNo }}</p>
            </div>
            <div class="next-box">
              <span class="mini-label">下一步</span>
              <strong>{{ nextActorText(record) }}</strong>
            </div>
          </div>

          <div class="chain-row">
            <div class="chain-node">
              <span class="mini-label">发起记录</span>
              <strong>实验室台账登记</strong>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-node">
              <span class="mini-label">确认人</span>
              <strong>{{ userName(record.calibrationUserId) }}</strong>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-node">
              <span class="mini-label">影响对象</span>
              <strong>{{ calibrationImpactText(record) }}</strong>
            </div>
          </div>

          <div class="detail-grid">
            <div class="detail-card">
              <div class="detail-title">校准时间</div>
              <p>校准日期：{{ formatDateTime(record.calibrationDate) }}</p>
              <p>有效期至：{{ formatDateTime(record.validUntil) }}</p>
              <p>结果：{{ calibrationResultText(record.calibrationResult) }}</p>
            </div>
            <div class="detail-card">
              <div class="detail-title">来源信息</div>
              <p>{{ calibrationSourceText(record) }}</p>
              <p v-if="record.remarks">备注来源：{{ record.remarks }}</p>
            </div>
          </div>

          <div class="link-grid">
            <div class="link-card">
              <div class="detail-title">借用联动</div>
              <template v-if="linkedBorrow(record)">
                <p>申请人：{{ userName(linkedBorrow(record)?.borrowerUserId) }}</p>
                <p>审批人：{{ userName(linkedBorrow(record)?.approverUserId) }}</p>
                <p>借用状态：{{ borrowStatusText(linkedBorrow(record)?.borrowStatus || 1) }}</p>
              </template>
              <p v-else>当前暂无借用记录，校准通过后设备可继续进入借用流程。</p>
            </div>
            <div class="link-card">
              <div class="detail-title">维修联动</div>
              <template v-if="linkedRepair(record)">
                <p>报修人：{{ userName(linkedRepair(record)?.reporterUserId) }}</p>
                <p>维修状态：{{ repairStatusText(linkedRepair(record)?.repairStatus) }}</p>
                <p>维修结果：{{ linkedRepair(record)?.repairResult || '待回填' }}</p>
              </template>
              <p v-else>若校准前检查发现异常，可直接补充维修记录并暂停借用。</p>
            </div>
          </div>

          <div class="footer-row">
            <span :class="['action-hint', { urgent: record.calibrationStatus !== 2 }]">{{ actionHint(record) }}</span>
            <el-space v-if="authStore.hasPermission('equipment_calibration:edit')" wrap>
              <el-button size="small" type="primary" plain @click="openConfirm(record)">确认校准</el-button>
              <el-button size="small" type="danger" plain @click="deleteRecord(record)">删除</el-button>
            </el-space>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="createVisible" class="calibration-dialog" title="新增校准任务" width="760px">
      <p class="dialog-tip">校准任务创建后会影响设备可用状态，并在到期提醒中显示给相关角色。</p>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="150px">
        <el-form-item label="实验室" prop="laboratoryId">
          <el-select v-model="createForm.laboratoryId" style="width: 100%">
            <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="equipmentId">
          <el-select v-model="createForm.equipmentId" filterable style="width: 100%">
            <el-option v-for="item in equipmentOptions" :key="item.id" :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label" :value="item.id" />
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
        <el-form-item label="来源说明">
          <el-input v-model="createForm.remarks" type="textarea" :rows="2" maxlength="255" placeholder="例如：来源于年度校准计划、维修后复检或到期提醒安排" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmVisible" class="calibration-dialog" title="确认校准结果" width="760px">
      <p class="dialog-tip">确认结果后会回写设备的最近校准和下次校准时间，并影响提醒中心与借用可用性。</p>
      <el-form ref="confirmFormRef" :model="confirmForm" :rules="confirmRules" label-width="150px">
        <el-form-item label="校准人员" prop="calibrationUserId">
          <el-select v-model="confirmForm.calibrationUserId" filterable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.id" :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label" :value="item.id" />
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
        <el-form-item label="来源说明">
          <el-input v-model="confirmForm.remarks" type="textarea" :rows="2" maxlength="255" placeholder="例如：来源于第三方检测报告、校准证书或现场确认单" show-word-limit />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  confirmEquipmentCalibration,
  createEquipmentCalibration,
  deleteEquipmentCalibration,
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
} from '../../api/modules/business'
import { useAuthStore } from '../../stores/auth'
import { borrowStatusText, formatDateTime } from './EquipmentBorrowBusinessHelpers'

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const confirmVisible = ref(false)
const currentId = ref<number>()
const records = ref<EquipmentCalibrationRecord[]>([])
const borrowRecords = ref<EquipmentBorrowRecord[]>([])
const repairRecords = ref<EquipmentRepairRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const laboratoryMap = ref<Record<string, string>>({})
const equipmentMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const confirmFormRef = ref<FormInstance>()

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

const workflowSteps = [
  { index: '01', title: '计划触发', desc: '来源可为年度计划、提醒到期或维修后复检。' },
  { index: '02', title: '人员确认', desc: '校准角色回填证书、日期、结果和来源证明。' },
  { index: '03', title: '状态反馈', desc: '结果同步影响设备借用、提醒和后续校准周期。' },
]

const pendingCount = computed(() => records.value.filter((item) => item.calibrationStatus === 1).length)
const borrowLinkedCount = computed(() => records.value.filter((item) => linkedBorrow(item)).length)
const reminderLinkedCount = computed(() => records.value.filter((item) => item.calibrationStatus !== 2).length)
const teacherBorrowSample = computed(() => borrowRecords.value.find((item) => isTeacherApplicant(item.borrowerUserId)))
const studentBorrowSample = computed(() => borrowRecords.value.find((item) => isStudentApplicant(item.borrowerUserId)))

function equipmentName(id?: number) {
  if (!id) return '-'
  return equipmentMap.value[String(id)] || String(id)
}

function laboratoryName(id?: number) {
  if (!id) return '-'
  return laboratoryMap.value[String(id)] || String(id)
}

function userName(id?: number) {
  if (!id) return '待确认'
  return userMap.value[String(id)] || String(id)
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

function calibrationResultText(result?: number) {
  return result === 1 ? '合格' : '不合格'
}

function calibrationStatusText(status?: number) {
  return status === 2 ? '已确认' : '待确认'
}

function calibrationStatusType(status?: number) {
  return status === 2 ? 'success' : 'warning'
}

function repairStatusText(status?: number) {
  if (status === 2) return '维修中'
  if (status === 3) return '已修复'
  if (status === 4) return '无法修复'
  return '待处理'
}

function linkedBorrow(record: EquipmentCalibrationRecord) {
  return borrowRecords.value.find((item) => item.equipmentId === record.equipmentId)
}

function linkedCalibrationByBorrow(borrow?: EquipmentBorrowRecord) {
  if (!borrow) return undefined
  return records.value.find((item) => item.equipmentId === borrow.equipmentId)
}

function linkedRepair(record: EquipmentCalibrationRecord) {
  return repairRecords.value.find((item) => item.equipmentId === record.equipmentId)
}

function calibrationSourceText(record: EquipmentCalibrationRecord) {
  return `证书 ${record.certificateNo} 对应设备 ${equipmentName(record.equipmentId)}，由 ${userName(record.calibrationUserId)} 负责确认。`
}

function nextActorText(record: EquipmentCalibrationRecord) {
  if (record.calibrationStatus !== 2) return '等待校准角色确认结果'
  if (record.calibrationResult === 2) return '设备暂停借用，必要时转维修'
  return '设备恢复正常，进入下一周期提醒'
}

function calibrationImpactText(record: EquipmentCalibrationRecord) {
  if (record.calibrationResult === 2) return '借用暂停与维修评估'
  return '设备借用与提醒中心'
}

function actionHint(record: EquipmentCalibrationRecord) {
  if (record.calibrationStatus !== 2) return '当前尚未确认，提醒中心应继续提示相关角色完成处理。'
  if (record.calibrationResult === 2) return '校准不合格，建议同步维修或限制借用。'
  return '校准已完成，设备可按新周期继续投入使用。'
}

function linkedCalibrationSummary(borrow?: EquipmentBorrowRecord) {
  const calibration = linkedCalibrationByBorrow(borrow)
  if (!calibration) return '当前未找到对应校准记录'
  return `证书 ${calibration.certificateNo}，状态 ${calibrationStatusText(calibration.calibrationStatus)}，有效期至 ${formatDateTime(calibration.validUntil)}`
}

function linkedCalibrationAction(borrow?: EquipmentBorrowRecord) {
  const calibration = linkedCalibrationByBorrow(borrow)
  if (!calibration) return '需要补充校准记录后再判断借用可用性'
  return calibration.calibrationStatus === 2 ? '校准已确认，设备可继续支撑申请链路' : '校准待确认，提醒中心需继续跟进相关角色'
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

async function loadOptions() {
  const [labs, equipment, users] = await Promise.all([fetchLaboratoryOptions(), fetchEquipmentOptions(), fetchUserOptions()])
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
    const [calibrationResult, borrowResult, repairResult] = await Promise.all([
      fetchEquipmentCalibrations({ current: 1, pageSize: 50 }),
      fetchEquipmentBorrows({ current: 1, pageSize: 50 }),
      fetchEquipmentRepairs({ current: 1, pageSize: 50 }),
    ])
    records.value = calibrationResult.data.records
    borrowRecords.value = borrowResult.data.records
    repairRecords.value = repairResult.data.records
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createEquipmentCalibration(createForm)
    ElMessage.success('校准任务已创建')
    createVisible.value = false
    await loadAll()
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
    await loadAll()
  } finally {
    saving.value = false
  }
}

async function deleteRecord(row: EquipmentCalibrationRecord) {
  const confirmed = await ElMessageBox.confirm(
    `确认删除 ${equipmentName(row.equipmentId)} 的校准记录吗？删除后会影响借用、提醒和维修联动。`,
    '确认删除校准记录',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' },
  ).catch(() => false)
  if (!confirmed) return
  await deleteEquipmentCalibration(row.id)
  ElMessage.success('校准记录已删除')
  await loadAll()
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadAll()])
})
</script>

<style scoped>
.flow-overview,
.sample-grid,
.detail-grid,
.link-grid,
.record-grid {
  display: grid;
  gap: 16px;
}
.flow-overview {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 20px;
}
.sample-grid,
.detail-grid,
.link-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}
.flow-card,
.metric-card,
.sample-showcase,
.calibration-card,
.calibration-dialog :deep(.el-dialog) {
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
.link-card p,
.dialog-tip,
.sample-line {
  margin: 6px 0 0;
  color: #a8b3c2;
  line-height: 1.68;
}
.metric-card,
.sample-showcase,
.calibration-card {
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
.metric-value.warning,
.action-hint.urgent {
  color: #8fdcff;
}
.sample-showcase {
  margin-bottom: 20px;
  padding: 22px;
}
.sample-section-head,
.toolbar,
.card-head,
.footer-row {
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
.record-grid {
  display: grid;
}
.calibration-card {
  padding: 18px;
}
.record-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
}
.next-box,
.chain-node,
.detail-card,
.link-card {
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
.footer-row {
  align-items: center;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed rgba(116, 145, 155, 0.18);
}
.action-hint {
  color: #a8b3c2;
  line-height: 1.6;
}
.calibration-dialog :deep(.el-dialog__header) {
  padding: 22px 24px 12px;
  border-bottom: 1px solid rgba(116, 145, 155, 0.12);
}
.calibration-dialog :deep(.el-dialog__title) {
  color: #f5f7fa;
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
}
.calibration-dialog :deep(.el-dialog__body) {
  padding: 24px;
  color: #dbe6f5;
}
.calibration-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 24px;
  border-top: 1px solid rgba(116, 145, 155, 0.12);
}
.calibration-dialog :deep(.el-form-item__label),
.calibration-dialog :deep(.el-radio__label),
.calibration-dialog :deep(.el-input__wrapper),
.calibration-dialog :deep(.el-textarea__inner),
.calibration-dialog :deep(.el-select__wrapper) {
  color: #dbe6f5;
}
.calibration-dialog :deep(.el-input__wrapper),
.calibration-dialog :deep(.el-textarea__inner),
.calibration-dialog :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 1px rgba(116, 145, 155, 0.14) inset;
}
@media (max-width: 1100px) {
  .flow-overview,
  .sample-grid,
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
  .card-head,
  .footer-row,
  .sample-section-head,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
