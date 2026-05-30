<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">{{ config.title }}</h2>
                <p class="page-subtitle">{{ config.subtitle }}</p>
            </div>
            <el-space>
                <el-input v-model="query.keyword" placeholder="请输入关键字" style="width: 240px" clearable />
                <el-button @click="loadData">查询</el-button>
                <el-button v-if="canCreate" type="primary" @click="handleCreate">
                    {{ config.createText || '新建记录' }}
                </el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="records" style="width: 100%">
                <el-table-column
                    v-for="column in config.columns"
                    :key="column.prop"
                    :label="column.label"
                    :min-width="column.width || 160"
                >
                    <template #default="{ row }">
                        {{ formatCell(row, column.prop) }}
                    </template>
                </el-table-column>
                <el-table-column label="状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag :type="Number(row.status ?? 1) === 1 ? 'success' : 'info'">
                            {{ Number(row.status ?? 1) === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column v-if="showActions" label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <el-space>
                            <el-button v-if="canEdit" size="small" @click="handleEdit(row)">编辑</el-button>
                            <el-popconfirm title="确认删除这条记录吗？" @confirm="handleDelete(Number(row.id))">
                                <template #reference>
                                    <el-button v-if="canDelete" size="small" type="danger" plain>删除</el-button>
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

        <el-dialog
            v-model="dialogVisible"
            :title="editingId ? `编辑${config.title}` : `新建${config.title}`"
            width="680px"
        >
            <el-form label-width="150px">
                <el-form-item v-for="column in config.columns" :key="column.prop" :label="column.label">
                    <el-select
                        v-if="column.type === 'select'"
                        v-model="formModel[column.prop]"
                        :placeholder="column.placeholder || `请选择${column.label}`"
                        style="width: 100%"
                    >
                        <el-option
                            v-for="option in column.options || []"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                        />
                    </el-select>
                    <el-input-number
                        v-else-if="column.type === 'number'"
                        v-model="formModel[column.prop]"
                        style="width: 100%"
                    />
                    <el-date-picker
                        v-else-if="column.type === 'datetime'"
                        v-model="formModel[column.prop]"
                        type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                    />
                    <el-input
                        v-else-if="column.type !== 'textarea'"
                        v-model="formModel[column.prop]"
                        :placeholder="column.placeholder || `请输入${column.label}`"
                    />
                    <el-input
                        v-else
                        v-model="formModel[column.prop]"
                        type="textarea"
                        :rows="3"
                        :placeholder="column.placeholder || `请输入${column.label}`"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createOne, deleteOne, fetchPage, updateOne } from '../../api/crud'
import { useAuthStore } from '../../stores/auth'
import type { CrudModuleConfig } from '../../types/crud'

const props = defineProps<{
    config: CrudModuleConfig<Record<string, unknown>>
}>()

const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const records = ref<Record<string, unknown>[]>([])
const editingId = ref<number | null>(null)
const authStore = useAuthStore()

const query = reactive({
    current: 1,
    pageSize: 10,
    keyword: '',
    total: 0,
})

const formModel = reactive<Record<string, unknown>>(props.config.emptyRecord())
const canCreate = computed(() => !props.config.createPermission || authStore.hasPermission(props.config.createPermission))
const canEdit = computed(() => !props.config.editPermission || authStore.hasPermission(props.config.editPermission))
const canDelete = computed(() => !props.config.deletePermission || authStore.hasPermission(props.config.deletePermission))
const showActions = computed(() => canEdit.value || canDelete.value)

function formatCell(row: Record<string, unknown>, prop: string) {
    const value = row[prop]
    const mapping = props.config.displayMappings?.[prop]
    if (mapping && value !== undefined && value !== null) {
        return mapping[String(value)] ?? value
    }
    return value ?? ''
}

function resetForm() {
    Object.assign(formModel, props.config.emptyRecord())
    editingId.value = null
}

async function loadData() {
    loading.value = true
    try {
        const result = await fetchPage<Record<string, unknown>>(props.config.endpoint, {
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
    if (!canCreate.value) {
        return
    }
    resetForm()
    dialogVisible.value = true
}

function handleEdit(record: Record<string, unknown>) {
    if (!canEdit.value) {
        return
    }
    Object.assign(formModel, record)
    editingId.value = Number(record.id)
    dialogVisible.value = true
}

async function handleSave() {
    saving.value = true
    try {
        if (editingId.value) {
            await updateOne(props.config.endpoint, editingId.value, formModel)
            ElMessage.success('更新成功')
        } else {
            await createOne(props.config.endpoint, formModel)
            ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        resetForm()
        await loadData()
    } finally {
        saving.value = false
    }
}

async function handleDelete(id: number) {
    if (!canDelete.value) {
        return
    }
    await deleteOne(props.config.endpoint, id)
    ElMessage.success('删除成功')
    await loadData()
}

function handlePageChange(page: number) {
    query.current = page
    loadData()
}

onMounted(loadData)
</script>
