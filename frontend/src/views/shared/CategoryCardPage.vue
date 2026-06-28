<template>
    <div class="category-page">
        <section class="page-header category-header">
            <div>
                <h2 class="page-title">{{ config.title }}</h2>
                <p class="page-subtitle">{{ config.subtitle }}</p>
            </div>

            <div class="table-tools category-tools">
                <el-input v-model="query.keyword" :placeholder="uiText.keywordPlaceholder" clearable class="category-search" />
                <el-button @click="loadData">查询</el-button>
                <el-button v-if="canCreate" type="primary" @click="handleCreate">
                    {{ config.createText || uiText.defaultCategoryCreateText }}
                </el-button>
            </div>
        </section>

        <section class="card-panel surface-highlight table-shell">
            <div class="table-head">
                <div class="table-head-title">
                    <div class="eyebrow">{{ uiText.recordEyebrow }}</div>
                    <h3>{{ uiText.categoryCardTitle }}</h3>
                    <p>{{ uiText.categoryCardDescription }}</p>
                </div>
                <div class="table-meta">
                    <div class="count-badge">
                        <span>{{ uiText.currentRecords }}</span>
                        <strong>{{ query.total }}</strong>
                    </div>
                    <div class="table-summary">共 {{ query.total }} 条记录</div>
                </div>
            </div>

            <div v-loading="loading" class="category-grid">
                <article v-for="record in records" :key="String(record.id)" class="category-card">
                    <div class="category-card-top">
                        <span class="data-code">{{ String(record[config.codeProp] ?? '') }}</span>
                        <el-tag class="status-tag" :type="Number(record.status ?? 1) === 1 ? 'success' : 'info'">
                            {{ Number(record.status ?? 1) === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </div>

                    <div class="category-card-main">
                        <h4>{{ String(record[config.nameProp] ?? uiText.unnamedCategory) }}</h4>
                        <p>{{ String(record[config.descriptionProp] ?? uiText.emptyCategoryDescription) }}</p>
                    </div>

                    <section class="category-related">
                        <div class="category-related-head">
                            <span>{{ config.relation.title }}</span>
                            <strong>{{ relatedItemsFor(record.id).length }}</strong>
                        </div>

                        <div v-if="relatedItemsFor(record.id).length" class="category-related-list">
                            <div
                                v-for="item in relatedItemsFor(record.id)"
                                :key="String(item.id)"
                                class="category-related-item"
                            >
                                <div class="category-related-main">
                                    <span class="data-code">{{ String(item[config.relation.codeProp] ?? '') }}</span>
                                    <strong>{{ String(item[config.relation.nameProp] ?? '') }}</strong>
                                </div>
                                <span v-if="relationMeta(item)" class="category-related-meta">
                                    {{ relationMeta(item) }}
                                </span>
                                <div v-if="showRelatedActions" class="category-related-actions action-group">
                                    <el-button
                                        v-if="canEditRelated"
                                        class="action-button"
                                        size="small"
                                        @click="handleRelatedEdit(item)"
                                    >
                                        编辑
                                    </el-button>
                                    <el-popconfirm title="确认删除这条记录吗？" @confirm="handleRelatedDelete(Number(item.id))">
                                        <template #reference>
                                            <el-button
                                                v-if="canDeleteRelated"
                                                class="action-button"
                                                size="small"
                                                type="danger"
                                                plain
                                            >
                                                删除
                                            </el-button>
                                        </template>
                                    </el-popconfirm>
                                </div>
                            </div>
                        </div>

                        <p v-else class="category-related-empty">
                            {{ config.relation.emptyText || uiText.emptyCategoryRecords }}
                        </p>
                    </section>

                    <div v-if="showActions" class="category-actions action-group">
                        <el-button v-if="canEdit" class="action-button" size="small" @click="handleEdit(record)">编辑</el-button>
                        <el-popconfirm title="确认删除这个分类吗？" @confirm="handleDelete(Number(record.id))">
                            <template #reference>
                                <el-button v-if="canDelete" class="action-button" size="small" type="danger" plain>删除</el-button>
                            </template>
                        </el-popconfirm>
                    </div>
                </article>
            </div>

            <div class="category-pagination">
                <el-pagination
                    background
                    layout="prev, pager, next, total"
                    :current-page="query.current"
                    :page-size="query.pageSize"
                    :total="query.total"
                    @current-change="handlePageChange"
                />
            </div>
        </section>

        <el-dialog
            v-model="dialogVisible"
            :title="editingId ? `编辑${config.title}` : `新增${config.title}`"
            width="680px"
        >
            <el-form label-width="120px" class="category-dialog-form">
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

        <el-dialog
            v-model="relatedDialogVisible"
            :title="`编辑${config.relation.title}`"
            width="760px"
        >
            <el-form label-width="120px" class="category-dialog-form">
                <el-form-item
                    v-for="column in config.relation.editor.columns"
                    :key="column.prop"
                    :label="column.label"
                >
                    <el-select
                        v-if="column.type === 'select'"
                        v-model="relatedFormModel[column.prop]"
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
                        v-model="relatedFormModel[column.prop]"
                        style="width: 100%"
                    />
                    <el-date-picker
                        v-else-if="column.type === 'datetime'"
                        v-model="relatedFormModel[column.prop]"
                        type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                    />
                    <el-input
                        v-else-if="column.type !== 'textarea'"
                        v-model="relatedFormModel[column.prop]"
                        :placeholder="column.placeholder || `请输入${column.label}`"
                    />
                    <el-input
                        v-else
                        v-model="relatedFormModel[column.prop]"
                        type="textarea"
                        :rows="3"
                        :placeholder="column.placeholder || `请输入${column.label}`"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-space>
                    <el-button @click="relatedDialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="relatedSaving" @click="handleRelatedSave">保存</el-button>
                </el-space>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createOne, deleteOne, fetchPage, updateOne } from '../../api/crud'
import { uiText } from '../../constants/uiText'
import { useAuthStore } from '../../stores/auth'
import type { CrudColumn, CrudModuleConfig } from '../../types/crud'
import { emitCrudSync, useCrudSync } from '../../utils/crudSync'

interface CategoryRelationConfig {
    endpoint: string
    foreignKey: string
    title: string
    codeProp: string
    nameProp: string
    metaProps?: string[]
    pageSize?: number
    emptyText?: string
    editor: CrudModuleConfig<Record<string, unknown>>
}

interface CategoryCardPageConfig {
    title: string
    subtitle: string
    endpoint: string
    codeProp: string
    nameProp: string
    descriptionProp: string
    relation: CategoryRelationConfig
    columns: CrudColumn[]
    emptyRecord: () => Record<string, unknown>
    createPermission?: string
    editPermission?: string
    deletePermission?: string
    createText?: string
    syncKeys?: string[]
}

const RELATION_PAGE_SIZE_LIMIT = 100

const props = defineProps<{
    config: CategoryCardPageConfig
}>()

const categorySyncKeys = props.config.syncKeys || [props.config.endpoint]
const relatedSyncKeys = props.config.relation.editor.syncKeys || [props.config.relation.editor.endpoint]
const pageSyncKeys = [...new Set([...categorySyncKeys, ...relatedSyncKeys])]
const syncSource = `category-card:${props.config.endpoint}:${Math.random().toString(36).slice(2)}`

const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const relatedDialogVisible = ref(false)
const relatedSaving = ref(false)
const editingId = ref<number | null>(null)
const relatedEditingId = ref<number | null>(null)
const records = ref<Record<string, unknown>[]>([])
const relatedRecords = ref<Record<string, unknown>[]>([])
const authStore = useAuthStore()

const query = reactive({
    current: 1,
    pageSize: 10,
    keyword: '',
    total: 0,
})

const formModel = reactive<Record<string, unknown>>(props.config.emptyRecord())
const relatedFormModel = reactive<Record<string, unknown>>(props.config.relation.editor.emptyRecord())
const canCreate = computed(() => !props.config.createPermission || authStore.hasPermission(props.config.createPermission))
const canEdit = computed(() => !props.config.editPermission || authStore.hasPermission(props.config.editPermission))
const canDelete = computed(() => !props.config.deletePermission || authStore.hasPermission(props.config.deletePermission))
const canEditRelated = computed(() => {
    const permission = props.config.relation.editor.editPermission
    return !permission || authStore.hasPermission(permission)
})
const canDeleteRelated = computed(() => {
    const permission = props.config.relation.editor.deletePermission
    return !permission || authStore.hasPermission(permission)
})
const showActions = computed(() => canEdit.value || canDelete.value)
const showRelatedActions = computed(() => canEditRelated.value || canDeleteRelated.value)
const relatedMap = computed(() => {
    const grouped: Record<string, Record<string, unknown>[]> = {}
    relatedRecords.value.forEach((item) => {
        const key = String(item[props.config.relation.foreignKey] ?? '')
        if (!grouped[key]) {
            grouped[key] = []
        }
        grouped[key].push(item)
    })
    return grouped
})

function relatedItemsFor(categoryId: unknown) {
    return relatedMap.value[String(categoryId ?? '')] ?? []
}

function relationMeta(item: Record<string, unknown>) {
    if (!props.config.relation.metaProps?.length) {
        return ''
    }

    return props.config.relation.metaProps
        .map((prop) => item[prop])
        .filter((value) => value !== undefined && value !== null && String(value).trim() !== '')
        .map((value) => String(value))
        .join(' / ')
}

function resetForm() {
    Object.assign(formModel, props.config.emptyRecord())
    editingId.value = null
}

function resetRelatedForm() {
    Object.assign(relatedFormModel, props.config.relation.editor.emptyRecord())
    relatedEditingId.value = null
}

async function loadPagedRecords(endpoint: string, pageSize: number) {
    const firstPage = await fetchPage<Record<string, unknown>>(endpoint, {
        current: 1,
        pageSize,
        keyword: '',
    })

    const allRecords = [...firstPage.data.records]
    const totalPages = firstPage.data.pages || Math.ceil(firstPage.data.total / pageSize)

    if (totalPages <= 1) {
        return allRecords
    }

    const pageRequests: Promise<Awaited<ReturnType<typeof fetchPage<Record<string, unknown>>>>>[] = []
    for (let page = 2; page <= totalPages; page += 1) {
        pageRequests.push(
            fetchPage<Record<string, unknown>>(endpoint, {
                current: page,
                pageSize,
                keyword: '',
            }),
        )
    }

    const pageResults = await Promise.all(pageRequests)
    pageResults.forEach((result) => {
        allRecords.push(...result.data.records)
    })

    return allRecords
}

async function loadData() {
    loading.value = true
    try {
        const categoryResult = await fetchPage<Record<string, unknown>>(props.config.endpoint, {
            current: query.current,
            pageSize: query.pageSize,
            keyword: query.keyword,
        })

        records.value = categoryResult.data.records
        query.total = categoryResult.data.total

        const relationPageSize = Math.min(
            props.config.relation.pageSize ?? RELATION_PAGE_SIZE_LIMIT,
            RELATION_PAGE_SIZE_LIMIT,
        )

        try {
            relatedRecords.value = await loadPagedRecords(props.config.relation.endpoint, relationPageSize)
        } catch (error) {
            relatedRecords.value = []
            ElMessage.error(uiText.categoryRelatedSyncFailed)
            console.error('Failed to load category related records', error)
        }
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

    resetForm()
    Object.assign(formModel, record)
    editingId.value = Number(record.id)
    dialogVisible.value = true
}

function handleRelatedEdit(record: Record<string, unknown>) {
    if (!canEditRelated.value) {
        return
    }

    resetRelatedForm()
    Object.assign(relatedFormModel, record)
    relatedEditingId.value = Number(record.id)
    relatedDialogVisible.value = true
}

async function handleSave() {
    saving.value = true
    try {
        if (editingId.value) {
            await updateOne(props.config.endpoint, editingId.value, formModel)
            ElMessage.success(uiText.updateSuccess)
        } else {
            await createOne(props.config.endpoint, formModel)
            ElMessage.success(uiText.createSuccess)
        }

        dialogVisible.value = false
        resetForm()
        await loadData()
        emitCrudSync(categorySyncKeys, syncSource)
    } finally {
        saving.value = false
    }
}

async function handleRelatedSave() {
    if (!relatedEditingId.value) {
        return
    }

    relatedSaving.value = true
    try {
        await updateOne(props.config.relation.editor.endpoint, relatedEditingId.value, relatedFormModel)
        ElMessage.success(uiText.updateSuccess)
        relatedDialogVisible.value = false
        resetRelatedForm()
        await loadData()
        emitCrudSync(relatedSyncKeys, syncSource)
    } finally {
        relatedSaving.value = false
    }
}

async function handleDelete(id: number) {
    if (!canDelete.value) {
        return
    }

    await deleteOne(props.config.endpoint, id)
    ElMessage.success(uiText.deleteSuccess)
    await loadData()
    emitCrudSync(categorySyncKeys, syncSource)
}

async function handleRelatedDelete(id: number) {
    if (!canDeleteRelated.value) {
        return
    }

    await deleteOne(props.config.relation.editor.endpoint, id)
    ElMessage.success(uiText.deleteSuccess)
    await loadData()
    emitCrudSync(relatedSyncKeys, syncSource)
}

function handlePageChange(page: number) {
    query.current = page
    void loadData()
}

useCrudSync(pageSyncKeys, loadData, syncSource)
onMounted(loadData)
</script>

<style scoped>
.category-page {
    display: grid;
    gap: 20px;
}

.category-header {
    margin-bottom: 0;
}

.category-tools {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.category-search {
    width: 260px;
}

.category-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 18px;
}

.category-card {
    display: grid;
    gap: 18px;
    padding: 20px;
    min-height: 260px;
    border-radius: 24px;
    border: 1px solid rgba(21, 49, 59, 0.08);
    background:
        linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 243, 236, 0.84)),
        linear-gradient(135deg, rgba(18, 127, 114, 0.06), rgba(212, 161, 76, 0.05));
    box-shadow: 0 18px 34px rgba(18, 34, 40, 0.08);
}

.category-card-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.category-card-main {
    display: grid;
    gap: 10px;
}

.category-card-main h4 {
    margin: 0;
    font-family: var(--font-display);
    font-size: 24px;
    line-height: 1.15;
    color: var(--text-main);
}

.category-card-main p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.75;
}

.category-related {
    display: grid;
    gap: 12px;
    padding: 16px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.62);
    border: 1px solid rgba(21, 49, 59, 0.08);
}

.category-related-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.category-related-head span {
    font-size: 12px;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--text-soft);
}

.category-related-head strong {
    font-family: var(--font-display);
    font-size: 18px;
    color: var(--accent-deep);
}

.category-related-list {
    display: grid;
    gap: 10px;
}

.category-related-item {
    display: grid;
    gap: 8px;
    padding: 12px 14px;
    border-radius: 16px;
    background: rgba(18, 127, 114, 0.04);
    border: 1px dashed rgba(18, 127, 114, 0.12);
}

.category-related-main {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
}

.category-related-main strong {
    color: var(--text-main);
    line-height: 1.5;
}

.category-related-meta {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.6;
}

.category-related-actions {
    justify-content: flex-start;
}

.category-related-empty {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

.category-actions {
    margin-top: auto;
}

.category-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 22px;
}

.category-dialog-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: var(--text-main);
}

@media (max-width: 1280px) {
    .category-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 900px) {
    .category-tools {
        justify-content: flex-start;
    }

    .category-search {
        width: 100%;
    }

    .category-grid {
        grid-template-columns: 1fr;
    }

    .category-pagination {
        justify-content: flex-start;
    }
}
</style>
