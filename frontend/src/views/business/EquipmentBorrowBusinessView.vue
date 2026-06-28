<template>
  <div>
    <div class="page-header">
      <div>
        <h2 class="page-title">设备借用协同</h2>
        <p class="page-subtitle">把申请人、审批人、归还检查人、提醒对象和异常维修串成一条链，避免借用流程与后续处理相互割裂。</p>
      </div>
      <el-space>
        <el-button @click="loadAll">刷新</el-button>
        <el-button v-if="authStore.hasPermission('equipment_borrow:edit')" type="primary" @click="openCreateDialog">新增借用</el-button>
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
          <div class="metric-label">当前借用中</div>
          <div class="metric-value">{{ borrowedCount }}</div>
          <div class="metric-note">需要继续跟踪归还时间</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="card-panel metric-card" shadow="never">
          <div class="metric-label">已逾期</div>
          <div class="metric-value danger">{{ overdueCount }}</div>
          <div class="metric-note">提醒页会同步显示待催还对象</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="card-panel metric-card" shadow="never">
          <div class="metric-label">归还后转维修</div>
          <div class="metric-value">{{ repairLinkedCount }}</div>
          <div class="metric-note">根据设备编码自动匹配维修记录</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="sample-showcase">
      <div class="sample-section-head">
        <div>
          <h3 class="section-title">申请样例直观展示</h3>
          <p class="section-desc">以下样例直接来自当前借用记录，用来对照教师与学生申请链路，不额外手工编造数据。</p>
        </div>
        <span class="sample-tip">来源：设备借用记录 + 用户账号标识 + 审批与联动结果</span>
      </div>
      <div class="sample-grid">
        <div v-if="teacherSample" class="sample-card teacher">
          <div class="sample-badge">教师申请样例</div>
          <div class="sample-title">{{ userName(teacherSample.borrowerUserId) }}申请 {{ equipmentName(teacherSample.equipmentId) }}</div>
          <p class="sample-line">用途：{{ teacherSample.purpose }}</p>
          <p class="sample-line">审批：{{ userName(teacherSample.approverUserId) }}</p>
          <p class="sample-line">时间：{{ formatDateTime(teacherSample.borrowDate) }} 至 {{ formatDateTime(teacherSample.dueDate) }}</p>
          <p class="sample-line">来源：{{ borrowSourceText(teacherSample) }}</p>
          <p class="sample-line">协同结果：{{ borrowActionHint(teacherSample) }}</p>
        </div>
        <div v-if="studentSample" class="sample-card student">
          <div class="sample-badge">学生申请样例</div>
          <div class="sample-title">{{ userName(studentSample.borrowerUserId) }}申请 {{ equipmentName(studentSample.equipmentId) }}</div>
          <p class="sample-line">用途：{{ studentSample.purpose }}</p>
          <p class="sample-line">审批：{{ userName(studentSample.approverUserId) }}</p>
          <p class="sample-line">时间：{{ formatDateTime(studentSample.borrowDate) }} 至 {{ formatDateTime(studentSample.dueDate) }}</p>
          <p class="sample-line">来源：{{ borrowSourceText(studentSample) }}</p>
          <p class="sample-line">协同结果：{{ borrowActionHint(studentSample) }}</p>
        </div>
      </div>
    </div>

    <el-card class="card-panel" shadow="never">
      <div class="toolbar">
        <div>
          <h3 class="section-title">借用协同记录</h3>
          <p class="section-desc">{{ recordsSummaryText }}</p>
        </div>
        <el-radio-group v-model="statusFilter" class="status-filter">
          <el-radio-button v-for="item in statusFilterOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div class="record-grid">
        <el-card v-for="record in filteredRecords" :key="record.id" class="borrow-card" shadow="never">
          <div class="borrow-card-head">
            <div>
              <div class="record-title">
                {{ equipmentName(record.equipmentId) }}
                <el-tag :type="borrowStatusType(record.borrowStatus)" effect="light">{{ borrowStatusText(record.borrowStatus) }}</el-tag>
              </div>
              <p class="record-subtitle">{{ laboratoryName(record.laboratoryId) }} · 申请用途：{{ record.purpose }}</p>
            </div>
            <div class="next-owner">
              <span class="next-label">下一步</span>
              <strong>{{ borrowNextActor(record) }}</strong>
            </div>
          </div>

          <div class="chain-row">
            <div class="chain-node">
              <span class="chain-label">申请人</span>
              <strong>{{ userName(record.borrowerUserId) }}</strong>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-node">
              <span class="chain-label">审批人</span>
              <strong>{{ userName(record.approverUserId) }}</strong>
            </div>
            <div class="chain-arrow">→</div>
            <div class="chain-node">
              <span class="chain-label">归还核验</span>
              <strong>{{ returnVerifierText(record) }}</strong>
            </div>
          </div>

          <div class="detail-panels">
            <div class="detail-block">
              <div class="detail-block-title">时间链路</div>
              <p>借用时间：{{ formatDateTime(record.borrowDate) }}</p>
              <p>应还时间：{{ formatDateTime(record.dueDate) }}</p>
              <p>实际归还：{{ formatDateTime(record.actualReturnDate) }}</p>
            </div>
            <div class="detail-block">
              <div class="detail-block-title">来源与依据</div>
              <p>{{ borrowSourceText(record) }}</p>
              <p v-if="record.returnCondition">归还检查：{{ record.returnCondition }}</p>
              <p v-if="record.remarks">备注来源：{{ record.remarks }}</p>
            </div>
          </div>

          <div class="link-panels">
            <div class="link-card">
              <div class="link-title">维修联动</div>
              <template v-if="linkedRepair(record)">
                <p>报修人：{{ userName(linkedRepair(record)?.reporterUserId) }}</p>
                <p>维修人：{{ userName(linkedRepair(record)?.repairUserId) }}</p>
                <p>维修状态：{{ repairStatusText(linkedRepair(record)?.repairStatus) }}</p>
              </template>
              <p v-else>当前暂无维修记录，若归还检查异常可直接转入维修流程。</p>
            </div>
            <div class="link-card">
              <div class="link-title">校准联动</div>
              <template v-if="linkedCalibration(record)">
                <p>校准人员：{{ userName(linkedCalibration(record)?.calibrationUserId) }}</p>
                <p>有效期至：{{ formatDateTime(linkedCalibration(record)?.validUntil) }}</p>
                <p>校准状态：{{ calibrationStatusText(linkedCalibration(record)?.calibrationStatus) }}</p>
              </template>
              <p v-else>当前未关联校准记录，若设备即将到期会在提醒页中转给相关角色。</p>
            </div>
          </div>

          <div class="action-row">
            <span :class="['action-hint', { urgent: record.borrowStatus === 4 }]">{{ borrowActionHint(record) }}</span>
            <el-space v-if="authStore.hasPermission('equipment_borrow:edit')" wrap>
              <el-button v-if="canReturn(record)" size="small" type="primary" plain @click="openReturnDialog(record)">登记归还</el-button>
              <el-button size="small" plain @click="openStatusDialog(record)">更新状态</el-button>
              <el-button
                v-if="record.borrowStatus === 4"
                size="small"
                type="danger"
                plain
                :loading="remindingId === record.id"
                @click="sendReminder(record)"
              >
                发送催还
              </el-button>
              <el-button size="small" type="danger" plain @click="deleteRecord(record)">删除</el-button>
            </el-space>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog v-model="createVisible" class="collab-dialog" title="新增设备借用" width="760px">
      <p class="dialog-tip">借用会同时记录申请人、审批人和时间依据，方便后续催还、归还检查和维修追踪。</p>
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
              :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人" prop="borrowerUserId">
          <el-select v-model="createForm.borrowerUserId" filterable placeholder="请选择申请人" style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人" prop="approverUserId">
          <el-select v-model="createForm.approverUserId" filterable placeholder="请选择审批人" style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="item.subLabel ? `${item.label}（${item.subLabel}）` : item.label"
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
        <el-form-item label="申请用途" prop="purpose">
          <el-input v-model="createForm.purpose" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="来源说明">
          <el-input v-model="createForm.remarks" type="textarea" :rows="2" maxlength="255" placeholder="例如：来源于课程实验计划、导师安排或项目申请单" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="returnVisible" class="collab-dialog" title="登记设备归还" width="620px">
      <p class="dialog-tip">归还检查结果会作为是否转入维修流程的直接依据，请明确填写检查情况。</p>
      <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="140px">
        <el-form-item label="借用记录">
          <el-input :model-value="selectedBorrowText" disabled />
        </el-form-item>
        <el-form-item label="归还检查" prop="returnCondition">
          <el-input v-model="returnForm.returnCondition" placeholder="例如：设备完好、附件齐全；或发现损坏需转维修" />
        </el-form-item>
        <el-form-item label="来源说明">
          <el-input
            v-model="returnForm.remarks"
            type="textarea"
            :rows="3"
            maxlength="255"
            placeholder="例如：由实验室主任现场核验，依据设备点检单登记"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="returnVisible = false">取消</el-button>
          <el-button type="primary" :loading="returning" @click="submitReturn">确认归还</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" class="collab-dialog" title="更新借用状态" width="620px">
      <p class="dialog-tip">适用于补录审批、逾期、拒绝和归还异常等场景，确保提醒、维修和统计口径保持一致。</p>
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="140px">
        <el-form-item label="借用记录">
          <el-input :model-value="selectedBorrowText" disabled />
        </el-form-item>
        <el-form-item label="借用状态" prop="borrowStatus">
          <el-select v-model="statusForm.borrowStatus" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" :value="1" />
            <el-option label="借用中" :value="2" />
            <el-option label="已归还" :value="3" />
            <el-option label="已逾期" :value="4" />
            <el-option label="已拒绝" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="statusForm.returnCondition" placeholder="例如：已联系申请人；归还后需送修；审批驳回原因" />
        </el-form-item>
        <el-form-item label="来源说明">
          <el-input
            v-model="statusForm.remarks"
            type="textarea"
            :rows="3"
            maxlength="255"
            placeholder="例如：来源于电话催还、现场核验或审批记录"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-space>
          <el-button @click="statusVisible = false">取消</el-button>
          <el-button type="primary" :loading="statusSaving" @click="submitStatus">保存</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createEquipmentBorrow,
  deleteEquipmentBorrow,
  fetchBorrowDetail,
  fetchEquipmentBorrows,
  fetchEquipmentCalibrations,
  fetchEquipmentOptions,
  fetchEquipmentRepairs,
  fetchLaboratoryOptions,
  fetchUserOptions,
  optionsToMap,
  returnEquipmentBorrow,
  sendEquipmentBorrowOverdueReminder,
  type EquipmentBorrowRecord,
  type EquipmentCalibrationRecord,
  type EquipmentRepairRecord,
  type SelectOption,
  updateEquipmentBorrowStatus,
} from '../../api/modules/business'
import { borrowStatusText, borrowStatusType, formatDateTime } from './EquipmentBorrowBusinessHelpers'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const returning = ref(false)
const statusSaving = ref(false)
const remindingId = ref<number>()
const createVisible = ref(false)
const returnVisible = ref(false)
const statusVisible = ref(false)
const statusFilter = ref(0)
const records = ref<EquipmentBorrowRecord[]>([])
const repairRecords = ref<EquipmentRepairRecord[]>([])
const calibrationRecords = ref<EquipmentCalibrationRecord[]>([])
const laboratoryOptions = ref<SelectOption[]>([])
const equipmentOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])
const laboratoryMap = ref<Record<string, string>>({})
const equipmentMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const createFormRef = ref<FormInstance>()
const returnFormRef = ref<FormInstance>()
const statusFormRef = ref<FormInstance>()
const currentBorrowId = ref<number>()

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

const statusForm = reactive({
  borrowStatus: 2,
  returnCondition: '',
  remarks: '',
})

const createRules: FormRules = {
  laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  borrowerUserId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
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
  purpose: [{ required: true, message: '请输入申请用途', trigger: 'blur' }],
}

const returnRules: FormRules = {
  returnCondition: [{ required: true, message: '请输入归还检查结果', trigger: 'blur' }],
}

const statusRules: FormRules = {
  borrowStatus: [{ required: true, message: '请选择借用状态', trigger: 'change' }],
}

const workflowSteps = [
  { index: '01', title: '申请登记', desc: '申请人提交借用目的、时间和来源依据，系统同步记录实验室与设备。' },
  { index: '02', title: '审批放行', desc: '审批人确认是否允许借出，并决定后续由谁跟进归还。' },
  { index: '03', title: '归还与联动', desc: '逾期进入催还，归还异常可直接串入维修，校准到期会同步进入提醒。' },
]

const statusFilterOptions = [
  { label: '全部', value: 0 },
  { label: '借用中', value: 2 },
  { label: '已逾期', value: 4 },
  { label: '已归还', value: 3 },
]

const borrowedCount = computed(() => records.value.filter((item) => item.borrowStatus === 2).length)
const overdueCount = computed(() => records.value.filter((item) => item.borrowStatus === 4).length)
const filteredRecords = computed(() => {
  if (!statusFilter.value) return records.value
  return records.value.filter((item) => item.borrowStatus === statusFilter.value)
})
const repairLinkedCount = computed(() => records.value.filter((item) => linkedRepair(item)).length)

const recordsSummaryText = computed(() => {
  if (!statusFilter.value) {
    return `共 ${records.value.length} 条借用链路，其中 ${overdueCount.value} 条需要优先催还，${repairLinkedCount.value} 条已联动到维修。`
  }
  return `当前筛选为“${borrowStatusText(statusFilter.value)}”，共 ${filteredRecords.value.length} 条记录。`
})

const selectedBorrowText = computed(() => {
  const borrow = records.value.find((item) => item.id === currentBorrowId.value)
  if (!borrow) return ''
  return `${equipmentName(borrow.equipmentId)} / 应还 ${formatDateTime(borrow.dueDate)}`
})

const teacherSample = computed(() => records.value.find((item) => isTeacherApplicant(item.borrowerUserId)))
const studentSample = computed(() => records.value.find((item) => isStudentApplicant(item.borrowerUserId)))

function equipmentName(equipmentId?: number) {
  if (!equipmentId) return '-'
  return equipmentMap.value[String(equipmentId)] || String(equipmentId)
}

function laboratoryName(laboratoryId?: number) {
  if (!laboratoryId) return '-'
  return laboratoryMap.value[String(laboratoryId)] || String(laboratoryId)
}

function userName(userId?: number) {
  if (!userId) return '待分配'
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

function repairStatusText(status?: number) {
  if (status === 2) return '维修中'
  if (status === 3) return '已修复'
  if (status === 4) return '无法修复'
  return '待处理'
}

function calibrationStatusText(status?: number) {
  if (status === 2) return '已确认'
  if (status === 1) return '待确认'
  return '待登记'
}

function returnVerifierText(record: EquipmentBorrowRecord) {
  if (record.borrowStatus === 3) return '实验室管理员已核验'
  if (record.borrowStatus === 4) return '等待归还核验'
  return '归还后登记'
}

function borrowNextActor(record: EquipmentBorrowRecord) {
  if (record.borrowStatus === 4) return `${userName(record.borrowerUserId)} 先归还，管理员跟进催还`
  if (record.borrowStatus === 2) return '申请人按期归还，管理员核验'
  if (record.borrowStatus === 3 && linkedRepair(record)) return `${userName(linkedRepair(record)?.repairUserId)} 继续维修处理`
  if (record.borrowStatus === 3) return '流程已闭环，可关注下次校准'
  if (record.borrowStatus === 5) return '申请人可补充材料后重新申请'
  return `${userName(record.approverUserId)} 处理审批`
}

function borrowSourceText(record: EquipmentBorrowRecord) {
  return [`申请人 ${userName(record.borrowerUserId)}`, `审批人 ${userName(record.approverUserId)}`, `依据借用用途“${record.purpose}”`].join('，')
}

function linkedRepair(record: EquipmentBorrowRecord) {
  return repairRecords.value.find((item) => item.equipmentId === record.equipmentId)
}

function linkedCalibration(record: EquipmentBorrowRecord) {
  return calibrationRecords.value.find((item) => item.equipmentId === record.equipmentId)
}

function canReturn(record: EquipmentBorrowRecord) {
  return record.borrowStatus === 2 || record.borrowStatus === 4
}

function borrowActionHint(record: EquipmentBorrowRecord) {
  if (record.borrowStatus === 4) return '已逾期，建议先发送催还通知，再登记归还结果。'
  if (record.borrowStatus === 2) return '借用进行中，需继续跟踪应还时间与设备状态。'
  if (record.borrowStatus === 3 && linkedRepair(record)) return '归还后发现异常，当前已串入维修流程。'
  if (record.borrowStatus === 3) return '借用流程已闭环，可转入常规校准与台账管理。'
  if (record.borrowStatus === 5) return '借用申请已拒绝，设备保持可用状态。'
  return '待审批或待补充说明。'
}

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
  returnForm.returnCondition = row.returnCondition || ''
  returnForm.remarks = row.remarks || ''
  returnVisible.value = true
}

function openStatusDialog(row: EquipmentBorrowRecord) {
  currentBorrowId.value = row.id
  statusForm.borrowStatus = row.borrowStatus
  statusForm.returnCondition = row.returnCondition || ''
  statusForm.remarks = row.remarks || ''
  statusVisible.value = true
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
    const [borrowResult, repairResult, calibrationResult] = await Promise.all([
      fetchEquipmentBorrows({ current: 1, pageSize: 50 }),
      fetchEquipmentRepairs({ current: 1, pageSize: 50 }),
      fetchEquipmentCalibrations({ current: 1, pageSize: 50 }),
    ])
    records.value = borrowResult.data.records
    repairRecords.value = repairResult.data.records
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
    await createEquipmentBorrow(createForm)
    ElMessage.success('借用申请已提交')
    createVisible.value = false
    await loadAll()
  } finally {
    saving.value = false
  }
}

async function submitReturn() {
  const valid = await returnFormRef.value?.validate().catch(() => false)
  if (!valid || !currentBorrowId.value) return
  returning.value = true
  try {
    await fetchBorrowDetail(currentBorrowId.value)
    await returnEquipmentBorrow(currentBorrowId.value, returnForm)
    ElMessage.success('设备已登记归还')
    returnVisible.value = false
    await loadAll()
  } finally {
    returning.value = false
  }
}

async function submitStatus() {
  const valid = await statusFormRef.value?.validate().catch(() => false)
  if (!valid || !currentBorrowId.value) return
  statusSaving.value = true
  try {
    await updateEquipmentBorrowStatus(currentBorrowId.value, statusForm)
    ElMessage.success('借用状态已更新')
    statusVisible.value = false
    await loadAll()
  } finally {
    statusSaving.value = false
  }
}

async function sendReminder(record: EquipmentBorrowRecord) {
  const equipment = equipmentName(record.equipmentId)
  const borrower = userName(record.borrowerUserId)
  const confirmed = await ElMessageBox.confirm(
    `将向 ${borrower} 发送“${equipment}”的逾期催还通知，并在提醒中心留下可追溯记录。`,
    '确认发送催还',
    {
      confirmButtonText: '发送催还',
      cancelButtonText: '取消',
      type: 'warning',
    },
  ).catch(() => false)
  if (!confirmed) return

  remindingId.value = record.id
  try {
    await sendEquipmentBorrowOverdueReminder(record.id, {
      message: `您借用的设备“${equipment}”已超过应还时间（${formatDateTime(record.dueDate)}），请尽快归还；如设备异常，请同步联系管理员转入维修。`,
    })
    ElMessage.success(`已向 ${borrower} 发送催还通知`)
  } finally {
    remindingId.value = undefined
  }
}

async function deleteRecord(record: EquipmentBorrowRecord) {
  const confirmed = await ElMessageBox.confirm(
    `确认删除 ${equipmentName(record.equipmentId)} 的借用记录吗？删除后会同步影响维修、校准与提醒联动。`,
    '确认删除借用记录',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    },
  ).catch(() => false)
  if (!confirmed) return

  await deleteEquipmentBorrow(record.id)
  ElMessage.success('借用记录已删除')
  await loadAll()
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadAll()])
})
</script>

<style scoped>
.flow-overview,
.sample-grid,
.detail-panels,
.link-panels,
.record-grid {
  display: grid;
  gap: 16px;
}

.flow-overview,
.sample-grid,
.detail-panels,
.link-panels {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.flow-overview {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 20px;
}

.flow-card,
.metric-card,
.sample-showcase,
.borrow-card,
.collab-dialog :deep(.el-dialog) {
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
.detail-block p,
.link-card p,
.dialog-tip,
.sample-line {
  margin: 6px 0 0;
  color: #a8b3c2;
  line-height: 1.68;
}

.metric-card,
.sample-showcase,
.borrow-card {
  border-radius: 20px;
}

.metric-label,
.next-label,
.chain-label,
.detail-block-title,
.link-title {
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

.metric-value.danger,
.action-hint.urgent {
  color: #ff8f8f;
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
.borrow-card-head,
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

.borrow-card {
  padding: 18px;
  border-radius: 24px;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
}

.next-owner,
.chain-node,
.detail-block,
.link-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(116, 145, 155, 0.12);
}

.next-owner strong,
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

.detail-block-title,
.link-title {
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

.dialog-tip {
  margin: 0 0 18px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(77, 155, 255, 0.08);
  border: 1px solid rgba(77, 155, 255, 0.14);
}

.collab-dialog :deep(.el-dialog__header) {
  padding: 22px 24px 12px;
  border-bottom: 1px solid rgba(116, 145, 155, 0.12);
}

.collab-dialog :deep(.el-dialog__title) {
  color: #f5f7fa;
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
}

.collab-dialog :deep(.el-dialog__body) {
  padding: 24px;
  color: #dbe6f5;
}

.collab-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 24px;
  border-top: 1px solid rgba(116, 145, 155, 0.12);
}

.collab-dialog :deep(.el-form-item__label),
.collab-dialog :deep(.el-radio-button__inner),
.collab-dialog :deep(.el-input__wrapper),
.collab-dialog :deep(.el-textarea__inner),
.collab-dialog :deep(.el-select__wrapper) {
  color: #dbe6f5;
}

.collab-dialog :deep(.el-input__wrapper),
.collab-dialog :deep(.el-textarea__inner),
.collab-dialog :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 1px rgba(116, 145, 155, 0.14) inset;
}

@media (max-width: 1100px) {
  .flow-overview,
  .sample-grid,
  .detail-panels,
  .link-panels {
    grid-template-columns: 1fr;
  }

  .chain-row {
    grid-template-columns: 1fr;
  }

  .chain-arrow {
    transform: rotate(90deg);
  }

  .borrow-card-head,
  .action-row,
  .toolbar,
  .sample-section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .next-owner {
    min-width: auto;
  }
}
</style>
