<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">操作日志</h2>
                <p class="page-subtitle">查看带审计注解的核心业务操作记录。</p>
            </div>
        </div>

        <el-card class="card-panel" shadow="never" style="margin-bottom: 16px">
            <el-form :inline="true">
                <el-form-item label="时间范围">
                    <el-date-picker
                        v-model="dateRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始时间"
                        end-placeholder="结束时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleFilter">查询</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="rows">
                <el-table-column prop="username" label="操作人" min-width="140" />
                <el-table-column prop="operation" label="操作描述" min-width="180" />
                <el-table-column prop="method" label="请求方法" min-width="220" />
                <el-table-column prop="ip" label="IP" min-width="140" />
                <el-table-column prop="createTime" label="操作时间" min-width="180" />
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
import { getAuditLogs, type AuditLogRow } from '../../api/modules/audit'

const loading = ref(false)
const rows = ref<AuditLogRow[]>([])
const dateRange = ref<[string, string] | []>([])

const query = reactive({
    current: 1,
    pageSize: 10,
    startDate: undefined as string | undefined,
    endDate: undefined as string | undefined,
    total: 0,
})

async function loadData() {
    loading.value = true
    try {
        const result = await getAuditLogs(query)
        rows.value = result.data.records
        query.total = result.data.total
    } finally {
        loading.value = false
    }
}

function handleFilter() {
    query.current = 1
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
    loadData()
}

function handlePageChange(page: number) {
    query.current = page
    loadData()
}

loadData()
</script>
