<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">用户管理</h2>
                <p class="page-subtitle">维护实验室平台的登录账号、基础信息与密码重置。</p>
            </div>
            <el-space>
                <el-input v-model="query.keyword" placeholder="请输入姓名、用户名或工号" style="width: 280px" clearable />
                <el-button @click="loadData">查询</el-button>
                <el-button v-permission="'user:edit'" type="primary" @click="handleCreate">新增用户</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="records">
                <el-table-column prop="username" label="用户名" min-width="140" />
                <el-table-column prop="realName" label="姓名" min-width="120" />
                <el-table-column prop="userNo" label="工号/学号" min-width="140" />
                <el-table-column prop="phone" label="手机号" min-width="140" />
                <el-table-column prop="email" label="邮箱" min-width="180" />
                <el-table-column label="所属实验室" min-width="140">
                    <template #default="{ row }">
                        {{ laboratoryMap[String(row.laboratoryId)] || row.laboratoryId || '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="用户类型" min-width="120">
                    <template #default="{ row }">
                        {{ userTypeMap[String(row.userType)] || row.userType }}
                    </template>
                </el-table-column>
                <el-table-column label="状态" min-width="100">
                    <template #default="{ row }">
                        <el-tag :type="Number(row.status ?? 1) === 1 ? 'success' : 'info'">
                            {{ Number(row.status ?? 1) === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="260" fixed="right">
                    <template #default="{ row }">
                        <el-space>
                            <el-button v-permission="'user:edit'" size="small" @click="handleEdit(row)">编辑</el-button>
                            <el-button v-permission="'user:edit'" size="small" type="warning" plain @click="openResetDialog(row)">
                                重置密码
                            </el-button>
                            <el-popconfirm title="确认删除该用户吗？" @confirm="handleDelete(Number(row.id))">
                                <template #reference>
                                    <el-button v-permission="'user:edit'" size="small" type="danger" plain>删除</el-button>
                                </template>
                            </el-popconfirm>
                        </el-space>
                    </template>
                </el-table-column>
            </el-table>

            <div style="display: flex; justify-content: flex-end; margin-top: 18px">
                <el-pagination
                    background
                    layout="prev, pager, next, total"
                    :current-page="query.current"
                    :page-size="query.pageSize"
                    :total="query.total"
                    @current-change="handlePageChange"
                />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="720px">
            <el-form :model="formModel" label-width="120px">
                <el-form-item label="所属实验室">
                    <el-select v-model="formModel.laboratoryId" placeholder="请选择实验室" style="width: 100%">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="用户名">
                    <el-input v-model="formModel.username" />
                </el-form-item>
                <el-form-item v-if="!editingId" label="初始密码">
                    <el-input v-model="formModel.initialPassword" type="password" show-password placeholder="请输入初始密码，长度不少于 6 位" />
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="formModel.realName" />
                </el-form-item>
                <el-form-item label="工号/学号">
                    <el-input v-model="formModel.userNo" />
                </el-form-item>
                <el-form-item label="手机号">
                    <el-input v-model="formModel.phone" />
                </el-form-item>
                <el-form-item label="邮箱">
                    <el-input v-model="formModel.email" />
                </el-form-item>
                <el-form-item label="用户类型">
                    <el-select v-model="formModel.userType" placeholder="请选择用户类型" style="width: 100%">
                        <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态">
                    <el-switch v-model="enabled" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
                </el-space>
            </template>
        </el-dialog>

        <el-dialog v-model="resetVisible" title="重置密码" width="480px">
            <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="100px">
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="resetForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="resetForm.confirmPassword" type="password" show-password />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="resetVisible = false">取消</el-button>
                    <el-button type="primary" :loading="resetSaving" @click="submitResetPassword">确认</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { deleteOne, fetchPage } from '../../api/crud'
import { fetchLaboratoryOptions, optionsToMap, type SelectOption } from '../../api/modules/business'
import { createUser, resetUserPassword, updateUser } from '../../api/modules/user'

interface UserRecord {
    id?: number
    laboratoryId: number
    username: string
    realName: string
    userNo: string
    phone: string
    email: string
    userType: number
    status: number
}

interface UserFormModel extends UserRecord {
    initialPassword: string
}

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const resetVisible = ref(false)
const resetSaving = ref(false)
const editingId = ref<number | null>(null)
const resetUserId = ref<number | null>(null)
const resetFormRef = ref<FormInstance>()
const records = ref<UserRecord[]>([])
const laboratoryMap = ref<Record<string, string>>({})
const laboratoryOptions = ref<SelectOption[]>([])
const userTypeOptions = [
    { label: '系统管理员', value: 1 },
    { label: '实验室主任', value: 2 },
    { label: '设备管理员', value: 3 },
    { label: '耗材管理员', value: 4 },
    { label: '危化品管理员', value: 5 },
    { label: '教师', value: 6 },
    { label: '学生', value: 7 },
    { label: '维修人员', value: 8 },
    { label: '校准人员', value: 9 },
]
const userTypeMap = userTypeOptions.reduce<Record<string, string>>((acc, item) => {
    acc[String(item.value)] = item.label
    return acc
}, {})

const query = reactive({
    current: 1,
    pageSize: 10,
    keyword: '',
    total: 0,
})

const formModel = reactive<UserFormModel>({
    laboratoryId: 1,
    username: '',
    initialPassword: '',
    realName: '',
    userNo: '',
    phone: '',
    email: '',
    userType: 6,
    status: 1,
})

const enabled = computed({
    get: () => Number(formModel.status ?? 1) === 1,
    set: (value: boolean) => {
        formModel.status = value ? 1 : 0
    },
})

const resetForm = reactive({
    newPassword: '',
    confirmPassword: '',
})

const resetRules: FormRules = {
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
            validator: (_rule, value, callback) => {
                if (value !== resetForm.newPassword) {
                    callback(new Error('两次输入的密码不一致'))
                    return
                }
                callback()
            },
            trigger: 'blur',
        },
    ],
}

function resetEditForm() {
    Object.assign(formModel, {
        laboratoryId: 1,
        username: '',
        initialPassword: '',
        realName: '',
        userNo: '',
        phone: '',
        email: '',
        userType: 6,
        status: 1,
    })
    editingId.value = null
}

async function loadLaboratoryMap() {
    const laboratories = await fetchLaboratoryOptions()
    laboratoryOptions.value = laboratories
    laboratoryMap.value = optionsToMap(laboratories)
}

async function loadData() {
    loading.value = true
    try {
        const result = await fetchPage<UserRecord>('/users', {
            current: query.current,
            pageSize: query.pageSize,
            keyword: query.keyword,
        })
        records.value = result.data.records
        query.total = result.data.total
    } finally {
        loading.value = false
    }
}

function handleCreate() {
    resetEditForm()
    dialogVisible.value = true
}

function handleEdit(record: UserRecord) {
    Object.assign(formModel, {
        laboratoryId: record.laboratoryId,
        username: record.username,
        initialPassword: '',
        realName: record.realName,
        userNo: record.userNo,
        phone: record.phone,
        email: record.email,
        userType: record.userType,
        status: record.status,
    })
    editingId.value = Number(record.id)
    dialogVisible.value = true
}

async function handleSave() {
    saving.value = true
    try {
        if (editingId.value) {
            await updateUser(editingId.value, {
                laboratoryId: formModel.laboratoryId,
                username: formModel.username,
                realName: formModel.realName,
                userNo: formModel.userNo,
                phone: formModel.phone,
                email: formModel.email,
                userType: formModel.userType,
                status: formModel.status,
            })
            ElMessage.success('用户更新成功')
        } else {
            await createUser({
                laboratoryId: formModel.laboratoryId,
                username: formModel.username,
                initialPassword: formModel.initialPassword,
                realName: formModel.realName,
                userNo: formModel.userNo,
                phone: formModel.phone,
                email: formModel.email,
                userType: formModel.userType,
                status: formModel.status,
            })
            ElMessage.success('用户创建成功')
        }
        dialogVisible.value = false
        resetEditForm()
        await loadData()
    } finally {
        saving.value = false
    }
}

async function handleDelete(id: number) {
    await deleteOne('/users', id)
    ElMessage.success('用户删除成功')
    await loadData()
}

function handlePageChange(page: number) {
    query.current = page
    loadData()
}

function openResetDialog(record: UserRecord) {
    resetUserId.value = Number(record.id)
    resetForm.newPassword = ''
    resetForm.confirmPassword = ''
    resetVisible.value = true
}

async function submitResetPassword() {
    const valid = await resetFormRef.value?.validate().catch(() => false)
    if (!valid || !resetUserId.value) {
        return
    }
    resetSaving.value = true
    try {
        await resetUserPassword(resetUserId.value, resetForm.newPassword)
        ElMessage.success('密码重置成功')
        resetVisible.value = false
    } finally {
        resetSaving.value = false
    }
}

onMounted(async () => {
    await Promise.all([loadLaboratoryMap(), loadData()])
})
</script>
