<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">角色与菜单</h2>
                <p class="page-subtitle">支持角色新增、编辑，以及菜单与按钮权限的后台配置。</p>
            </div>
            <el-space>
                <el-button @click="loadAll">刷新</el-button>
                <el-button v-permission="'role:edit'" type="primary" @click="openRoleDialog()">新增角色</el-button>
                <el-button
                    v-permission="'role:edit'"
                    type="success"
                    :loading="saving"
                    :disabled="!selectedRoleId"
                    @click="submitAssignments"
                >
                    保存菜单分配
                </el-button>
            </el-space>
        </div>

        <el-row :gutter="20">
            <el-col :xs="24" :lg="8">
                <el-card class="card-panel" shadow="never">
                    <template #header>
                        <div style="font-weight: 700">角色列表</div>
                    </template>
                    <el-table
                        v-loading="loadingRoles"
                        :data="roles"
                        highlight-current-row
                        style="width: 100%"
                        @current-change="handleRoleChange"
                    >
                        <el-table-column label="角色名称" min-width="140">
                            <template #default="{ row }">
                                <span>{{ getRoleDisplayName(row) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="roleCode" label="角色编码" min-width="160" />
                        <el-table-column label="操作" width="100">
                            <template #default="{ row }">
                                <el-button v-permission="'role:edit'" size="small" @click.stop="openRoleDialog(row)">
                                    编辑
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>

            <el-col :xs="24" :lg="16">
                <el-card class="card-panel" shadow="never">
                    <template #header>
                        <div style="display: flex; justify-content: space-between; align-items: center">
                            <span style="font-weight: 700">菜单分配</span>
                            <span style="color: var(--text-secondary); font-size: 13px">
                                {{ selectedRoleLabel || '请选择一个角色后进行授权' }}
                            </span>
                        </div>
                    </template>
                    <el-tree
                        ref="treeRef"
                        node-key="id"
                        show-checkbox
                        check-strictly
                        :data="menuTree"
                        :props="treeProps"
                        default-expand-all
                        empty-text="暂无菜单数据"
                    >
                        <template #default="{ data }">
                            <div style="display: flex; align-items: center; gap: 10px">
                                <span>{{ data.menuName }}</span>
                                <el-tag size="small" effect="plain">
                                    {{ data.menuType === 1 ? '菜单' : '权限' }}
                                </el-tag>
                                <span style="font-size: 12px; color: var(--text-secondary)">
                                    {{ data.permissionCode || data.routePath }}
                                </span>
                            </div>
                        </template>
                    </el-tree>
                </el-card>
            </el-col>
        </el-row>

        <el-dialog v-model="dialogVisible" :title="editingRoleId ? '编辑角色' : '新增角色'" width="520px">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="form.roleName" placeholder="请输入角色名称" />
                </el-form-item>
                <el-form-item label="角色编码" prop="roleCode">
                    <el-input
                        v-model="form.roleCode"
                        :disabled="Boolean(editingRoleId)"
                        placeholder="请输入角色编码"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="submittingRole" @click="submitRole">保存</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type ElTree, type FormInstance, type FormRules } from 'element-plus'
import { roleLabels } from '../../constants/roles'
import {
    assignRoleMenus,
    createRole,
    fetchMenuTree,
    fetchRoleMenuIds,
    fetchRoles,
    updateRole,
    type RoleItem,
} from '../../api/modules/role'
import type { MenuTreeItem } from '../../types/auth'

const loadingRoles = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const submittingRole = ref(false)
const roles = ref<RoleItem[]>([])
const menuTree = ref<MenuTreeItem[]>([])
const selectedRoleId = ref<number>()
const editingRoleId = ref<number | null>(null)
const treeRef = ref<InstanceType<typeof ElTree>>()
const formRef = ref<FormInstance>()

const form = reactive({
    roleName: '',
    roleCode: '',
})

const rules: FormRules = {
    roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

const treeProps = {
    children: 'children',
    label: 'menuName',
}

const selectedRoleLabel = computed(() => {
    const role = roles.value.find((item) => item.id === selectedRoleId.value)
    return role ? getRoleDisplayName(role) : ''
})

function getRoleDisplayName(role: RoleItem) {
    return roleLabels[role.roleCode] || role.roleName || role.roleCode
}

async function loadRoles() {
    loadingRoles.value = true
    try {
        const result = await fetchRoles()
        roles.value = result.data.filter((role) => role.status === 1)
        if (!selectedRoleId.value && roles.value.length > 0) {
            selectedRoleId.value = roles.value[0].id
        }
    } finally {
        loadingRoles.value = false
    }
}

async function loadMenuTree() {
    const result = await fetchMenuTree()
    menuTree.value = result.data
}

async function loadRoleSelection(roleId?: number) {
    if (!roleId) {
        treeRef.value?.setCheckedKeys([])
        return
    }
    const result = await fetchRoleMenuIds(roleId)
    treeRef.value?.setCheckedKeys(result.data, false)
}

async function handleRoleChange(role?: RoleItem) {
    selectedRoleId.value = role?.id
    await loadRoleSelection(role?.id)
}

async function submitAssignments() {
    if (!selectedRoleId.value) {
        return
    }
    saving.value = true
    try {
        const checkedKeys = (treeRef.value?.getCheckedKeys(false) ?? []) as number[]
        await assignRoleMenus(selectedRoleId.value, checkedKeys)
        ElMessage.success('角色菜单分配已保存')
    } finally {
        saving.value = false
    }
}

function openRoleDialog(role?: RoleItem) {
    editingRoleId.value = role?.id ?? null
    form.roleName = role?.roleName ?? ''
    form.roleCode = role?.roleCode ?? ''
    dialogVisible.value = true
}

async function submitRole() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    submittingRole.value = true
    try {
        if (editingRoleId.value) {
            await updateRole(editingRoleId.value, form)
            ElMessage.success('角色更新成功')
        } else {
            await createRole(form)
            ElMessage.success('角色创建成功')
        }
        dialogVisible.value = false
        await loadRoles()
    } finally {
        submittingRole.value = false
    }
}

async function loadAll() {
    await Promise.all([loadRoles(), loadMenuTree()])
    await loadRoleSelection(selectedRoleId.value)
}

onMounted(loadAll)
</script>
