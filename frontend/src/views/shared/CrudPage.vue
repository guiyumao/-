<template>
    <div class="crud-page">
        <section class="page-header crud-header">
            <div>
                <h2 class="page-title">{{ config.title }}</h2>
                <p class="page-subtitle">{{ config.subtitle }}</p>
            </div>

            <div class="table-tools crud-actions">
                <el-input v-model="query.keyword" :placeholder="uiText.keywordPlaceholder" clearable class="crud-search" />
                <el-button @click="loadData">查询</el-button>
                <el-button v-if="canCreate" type="primary" @click="handleCreate">
                    {{ config.createText || uiText.defaultCreateText }}
                </el-button>
            </div>
        </section>

        <section class="card-panel surface-highlight table-shell crud-table-panel">
            <div class="table-head crud-table-head">
                <div class="table-head-title">
                    <div class="eyebrow">{{ uiText.recordEyebrow }}</div>
                    <h3>{{ isCardLayout ? uiText.cardViewTitle : uiText.tableViewTitle }}</h3>
                    <p>
                        {{
                            isCardLayout
                                ? uiText.cardViewDescription
                                : uiText.tableViewDescription
                        }}
                    </p>
                </div>
                <div class="table-meta">
                    <div class="count-badge">
                        <span>{{ uiText.currentRecords }}</span>
                        <strong>{{ query.total }}</strong>
                    </div>
                    <div class="table-summary">共 {{ query.total }} 条记录</div>
                </div>
            </div>

            <div v-if="isCardLayout" v-loading="loading" class="crud-card-grid">
                <article v-for="row in records" :key="String(row.id)" class="crud-record-card">
                    <div class="crud-record-top">
                        <span v-if="cardCodeValue(row)" class="data-code">{{ cardCodeValue(row) }}</span>
                        <el-tag class="status-tag" :type="Number(row.status ?? 1) === 1 ? 'success' : 'info'">
                            {{ Number(row.status ?? 1) === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </div>

                    <div class="crud-record-main">
                        <h4>{{ cardTitleValue(row) || uiText.unnamedRecord }}</h4>
                        <p>{{ cardDescriptionValue(row) || uiText.emptyDescription }}</p>
                    </div>

                    <div v-if="cardDetailColumns.length" class="crud-record-fields">
                        <div v-for="column in cardDetailColumns" :key="column.prop" class="crud-record-field">
                            <span>{{ column.label }}</span>
                            <strong :class="getCellClass(column.prop)">
                                {{ formatCell(row, column.prop) || '-' }}
                            </strong>
                        </div>
                    </div>

                    <section v-if="config.cardRelation" class="crud-related-block">
                        <div class="crud-related-head">
                            <span>{{ config.cardRelation.title }}</span>
                            <strong>{{ relatedItemsFor(row).length }}</strong>
                        </div>

                        <div v-if="relatedItemsFor(row).length" class="crud-related-list">
                            <div
                                v-for="item in relatedItemsFor(row)"
                                :key="String(item.id ?? `${relationCodeValue(item)}-${relationNameValue(item)}`)"
                                class="crud-related-item"
                            >
                                <div class="crud-related-item-main">
                                    <span class="data-code">{{ relationCodeValue(item) }}</span>
                                    <strong>{{ relationNameValue(item) }}</strong>
                                </div>
                                <span v-if="relationMetaValue(item)" class="crud-related-item-meta">
                                    {{ relationMetaValue(item) }}
                                </span>
                            </div>
                        </div>

                        <p v-else class="crud-related-empty">
                            {{ config.cardRelation.emptyText || uiText.emptyRelatedRecords }}
                        </p>
                    </section>

                    <div v-if="showActions" class="crud-record-actions action-group">
                        <el-button v-if="canEdit" class="action-button" size="small" @click="handleEdit(row)">编辑</el-button>
                        <el-popconfirm title="确认删除这条记录吗？" @confirm="handleDelete(Number(row.id))">
                            <template #reference>
                                <el-button v-if="canDelete" class="action-button" size="small" type="danger" plain>
                                    删除
                                </el-button>
                            </template>
                        </el-popconfirm>
                    </div>
                </article>
            </div>

            <el-table v-else v-loading="loading" :data="records" class="crud-table">
                <el-table-column
                    v-for="column in config.columns"
                    :key="column.prop"
                    :label="column.label"
                    :min-width="column.width || 160"
                >
                    <template #default="{ row }">
                        <span :class="getCellClass(column.prop)">
                            {{ formatCell(row, column.prop) }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="状态" min-width="120">
                    <template #default="{ row }">
                        <el-tag class="status-tag" :type="Number(row.status ?? 1) === 1 ? 'success' : 'info'">
                            {{ Number(row.status ?? 1) === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column v-if="showActions" label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <div class="action-group">
                            <el-button v-if="canEdit" class="action-button" size="small" @click="handleEdit(row)">编辑</el-button>
                            <el-popconfirm title="确认删除这条记录吗？" @confirm="handleDelete(Number(row.id))">
                                <template #reference>
                                    <el-button v-if="canDelete" class="action-button" size="small" type="danger" plain>
                                        删除
                                    </el-button>
                                </template>
                            </el-popconfirm>
                        </div>
                    </template>
                </el-table-column>
            </el-table>

            <div class="crud-pagination">
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
            <el-form label-width="120px" class="crud-dialog-form">
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
import { uiText } from '../../constants/uiText'
import { useAuthStore } from '../../stores/auth'
import type { CrudCardRelationConfig, CrudModuleConfig } from '../../types/crud'
import { emitCrudSync, useCrudSync } from '../../utils/crudSync'

const RELATION_PAGE_SIZE_LIMIT = 100

const props = defineProps<{
    config: CrudModuleConfig<Record<string, unknown>>
}>()

const syncKeys = props.config.syncKeys || [props.config.endpoint]
const syncSource = `crud-page:${props.config.endpoint}:${Math.random().toString(36).slice(2)}`

const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const records = ref<Record<string, unknown>[]>([])
const relatedRecords = ref<Record<string, unknown>[]>([])
const editingId = ref<number | null>(null)
const authStore = useAuthStore()

const query = reactive({
    current: 1,
    pageSize: 10,
    keyword: '',
    total: 0,
})

const formModel = reactive<Record<string, unknown>>(props.config.emptyRecord())
const isCardLayout = computed(() => props.config.layout === 'cards')
const canCreate = computed(() => !props.config.createPermission || authStore.hasPermission(props.config.createPermission))
const canEdit = computed(() => !props.config.editPermission || authStore.hasPermission(props.config.editPermission))
const canDelete = computed(() => !props.config.deletePermission || authStore.hasPermission(props.config.deletePermission))
const showActions = computed(() => canEdit.value || canDelete.value)
const cardCodeColumn = computed(() => props.config.columns[0])
const cardTitleColumn = computed(() => props.config.columns[1] ?? props.config.columns[0])
const cardDescriptionColumn = computed(() => props.config.columns[2])
const cardDetailColumns = computed(() => props.config.columns.slice(3))
const relatedItemsMap = computed(() => {
    const relation = props.config.cardRelation
    const grouped: Record<string, Record<string, unknown>[]> = {}
    if (!relation) {
        return grouped
    }

    relatedRecords.value.forEach((item) => {
        const key = String(item[relation.foreignKey] ?? '')
        if (!grouped[key]) {
            grouped[key] = []
        }
        grouped[key].push(item)
    })

    return grouped
})

function formatCell(row: Record<string, unknown>, prop: string) {
    const value = row[prop]
    const mapping = props.config.displayMappings?.[prop]
    if (mapping && value !== undefined && value !== null) {
        return mapping[String(value)] ?? value
    }
    return value ?? ''
}

function getCellClass(prop: string) {
    const normalized = prop.toLowerCase()
    if (normalized.includes('code') || normalized.endsWith('no')) {
        return 'data-code'
    }
    if (normalized.includes('description') || normalized.includes('remark')) {
        return 'data-secondary'
    }
    return ''
}

function cardCodeValue(row: Record<string, unknown>) {
    const prop = cardCodeColumn.value?.prop
    return prop ? String(formatCell(row, prop) ?? '') : ''
}

function cardTitleValue(row: Record<string, unknown>) {
    const prop = cardTitleColumn.value?.prop
    return prop ? String(formatCell(row, prop) ?? '') : ''
}

function cardDescriptionValue(row: Record<string, unknown>) {
    const prop = cardDescriptionColumn.value?.prop
    return prop ? String(formatCell(row, prop) ?? '') : ''
}

function relationCodeValue(item: Record<string, unknown>) {
    const relation = props.config.cardRelation
    return relation ? String(item[relation.itemCodeProp] ?? '') : ''
}

function relationNameValue(item: Record<string, unknown>) {
    const relation = props.config.cardRelation
    return relation ? String(item[relation.itemNameProp] ?? '') : ''
}

function relationMetaValue(item: Record<string, unknown>) {
    const relation = props.config.cardRelation
    if (!relation?.itemMetaProps?.length) {
        return ''
    }

    return relation.itemMetaProps
        .map((prop) => item[prop])
        .filter((value) => value !== undefined && value !== null && String(value).trim() !== '')
        .map((value) => String(value))
        .join(' / ')
}

function relatedItemsFor(row: Record<string, unknown>) {
    return relatedItemsMap.value[String(row.id ?? '')] ?? []
}

function resetForm() {
    Object.assign(formModel, props.config.emptyRecord())
    editingId.value = null
}

async function loadRelatedData(relation: CrudCardRelationConfig) {
    const pageSize = Math.min(relation.pageSize ?? RELATION_PAGE_SIZE_LIMIT, RELATION_PAGE_SIZE_LIMIT)
    const firstPage = await fetchPage<Record<string, unknown>>(relation.endpoint, {
        current: 1,
        pageSize,
        keyword: '',
    })

    const allRecords = [...firstPage.data.records]
    const totalPages = firstPage.data.pages || Math.ceil(firstPage.data.total / pageSize)

    if (totalPages <= 1) {
        relatedRecords.value = allRecords
        return
    }

    const pageRequests: Promise<Awaited<ReturnType<typeof fetchPage<Record<string, unknown>>>>>[] = []
    for (let page = 2; page <= totalPages; page += 1) {
        pageRequests.push(
            fetchPage<Record<string, unknown>>(relation.endpoint, {
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

    relatedRecords.value = allRecords
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

        if (isCardLayout.value && props.config.cardRelation) {
            try {
                await loadRelatedData(props.config.cardRelation)
            } catch (error) {
                relatedRecords.value = []
                ElMessage.error(uiText.relatedSyncFailed)
                console.error('Failed to load related records', error)
            }
        } else {
            relatedRecords.value = []
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
        emitCrudSync(syncKeys, syncSource)
    } finally {
        saving.value = false
    }
}

async function handleDelete(id: number) {
    if (!canDelete.value) {
        return
    }

    await deleteOne(props.config.endpoint, id)
    ElMessage.success(uiText.deleteSuccess)
    await loadData()
    emitCrudSync(syncKeys, syncSource)
}

function handlePageChange(page: number) {
    query.current = page
    void loadData()
}

useCrudSync(syncKeys, loadData, syncSource)
onMounted(loadData)
</script>

<style scoped>
.crud-page {
    display: grid;
    gap: 20px;
}

.crud-header {
    margin-bottom: 0;
}

.crud-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.crud-search {
    width: 260px;
}

.crud-table-panel {
    position: relative;
}

.crud-table {
    border: 1px solid rgba(21, 49, 59, 0.07);
}

.crud-card-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 18px;
}

.crud-record-card {
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
    transition:
        transform 0.18s ease,
        box-shadow 0.18s ease,
        border-color 0.18s ease;
}

.crud-record-card:hover {
    transform: translateY(-4px);
    border-color: rgba(18, 127, 114, 0.18);
    box-shadow: 0 24px 44px rgba(18, 34, 40, 0.12);
}

.crud-record-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.crud-record-main {
    display: grid;
    gap: 10px;
}

.crud-record-main h4 {
    margin: 0;
    font-family: var(--font-display);
    font-size: 24px;
    line-height: 1.15;
    color: var(--text-main);
}

.crud-record-main p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.75;
}

.crud-record-fields {
    display: grid;
    gap: 10px;
}

.crud-record-field {
    display: grid;
    gap: 6px;
    padding-top: 10px;
    border-top: 1px dashed rgba(21, 49, 59, 0.12);
}

.crud-record-field span {
    font-size: 12px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--text-soft);
}

.crud-record-field strong {
    color: var(--text-main);
    line-height: 1.65;
}

.crud-related-block {
    display: grid;
    gap: 12px;
    padding: 16px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.62);
    border: 1px solid rgba(21, 49, 59, 0.08);
}

.crud-related-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.crud-related-head span {
    font-size: 12px;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--text-soft);
}

.crud-related-head strong {
    font-family: var(--font-display);
    font-size: 18px;
    color: var(--accent-deep);
}

.crud-related-list {
    display: grid;
    gap: 10px;
}

.crud-related-item {
    display: grid;
    gap: 8px;
    padding: 12px 14px;
    border-radius: 16px;
    background: rgba(18, 127, 114, 0.04);
    border: 1px dashed rgba(18, 127, 114, 0.12);
}

.crud-related-item-main {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
}

.crud-related-item-main strong {
    color: var(--text-main);
    line-height: 1.5;
}

.crud-related-item-meta {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.6;
}

.crud-related-empty {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

.crud-record-actions {
    margin-top: auto;
}

.crud-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 22px;
}

.crud-dialog-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: var(--text-main);
}

@media (max-width: 1280px) {
    .crud-card-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 900px) {
    .crud-actions {
        justify-content: flex-start;
    }

    .crud-search {
        width: 100%;
    }

    .crud-card-grid {
        grid-template-columns: 1fr;
    }

    .crud-pagination {
        justify-content: flex-start;
    }
}
</style>
