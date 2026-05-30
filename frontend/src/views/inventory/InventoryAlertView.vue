<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">库存预警</h2>
                <p class="page-subtitle">实时查看低于安全库存的耗材项目，支持分页查询。</p>
            </div>
            <el-space>
                <el-input v-model="query.keyword" placeholder="请输入耗材名称或编码" style="width: 280px" clearable />
                <el-button @click="loadData">查询</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="rows" :row-class-name="rowClassName">
                <el-table-column prop="consumableCode" label="耗材编码" min-width="140" />
                <el-table-column prop="consumableName" label="耗材名称" min-width="180" />
                <el-table-column prop="currentStock" label="当前库存" min-width="120" />
                <el-table-column prop="safetyStock" label="安全库存" min-width="120" />
                <el-table-column prop="storageLocation" label="存放位置" min-width="180" />
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
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { getInventoryAlerts, type InventoryAlertRow } from '../../api/modules/inventory'

const loading = ref(false)
const rows = ref<InventoryAlertRow[]>([])

const query = reactive({
    current: 1,
    pageSize: 10,
    keyword: '',
    total: 0,
})

async function loadData() {
    loading.value = true
    try {
        const result = await getInventoryAlerts(query)
        rows.value = result.data.records
        query.total = result.data.total
    } finally {
        loading.value = false
    }
}

function handlePageChange(page: number) {
    query.current = page
    loadData()
}

function rowClassName() {
    return 'danger-row'
}

loadData()
</script>

<style scoped>
:deep(.danger-row) {
    --el-table-tr-bg-color: rgba(200, 85, 61, 0.08);
}
</style>
