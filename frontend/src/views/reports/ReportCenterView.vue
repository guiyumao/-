<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">报表中心</h2>
                <p class="page-subtitle">支持按实验室、时间范围和业务类型筛选报表，并导出当前结果。</p>
            </div>
            <el-space>
                <el-button @click="loadData">刷新</el-button>
                <el-button v-permission="'report:export'" type="primary" @click="handleExport">导出 CSV</el-button>
            </el-space>
        </div>

        <el-card class="card-panel" shadow="never" style="margin-bottom: 16px">
            <el-form :inline="true" :model="filters">
                <el-form-item label="实验室">
                    <el-select v-model="filters.labId" clearable placeholder="全部实验室" style="width: 220px">
                        <el-option v-for="item in laboratoryOptions" :key="item.id" :label="item.label" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="日期范围">
                    <el-date-picker
                        v-model="dateRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始时间"
                        end-placeholder="结束时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                    />
                </el-form-item>
                <el-form-item label="业务类型">
                    <el-select v-model="filters.type" clearable placeholder="全部类型" style="width: 220px">
                        <el-option label="设备借用" value="borrow" />
                        <el-option label="设备维修" value="repair" />
                        <el-option label="设备校准" value="calibration" />
                        <el-option label="耗材出库" value="consumable" />
                        <el-option label="危化品业务" value="hazardous" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleFilterChange">筛选</el-button>
                    <el-button @click="resetFilters">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card class="card-panel" shadow="never">
            <el-table v-loading="loading" :data="rows">
                <el-table-column label="业务类型" min-width="150">
                    <template #default="{ row }">
                        {{ reportTypeMap[row.reportType] || row.reportType }}
                    </template>
                </el-table-column>
                <el-table-column prop="itemCode" label="物品编码" min-width="140" />
                <el-table-column prop="itemName" label="物品名称" min-width="180" />
                <el-table-column prop="laboratoryName" label="实验室" min-width="160" />
                <el-table-column prop="businessDate" label="业务日期" min-width="180" />
                <el-table-column prop="quantity" label="数量" min-width="100" />
                <el-table-column prop="amount" label="金额" min-width="110" />
                <el-table-column prop="statusText" label="状态" min-width="100" />
            </el-table>

            <div style="display: flex; justify-content: flex-end; margin-top: 18px">
                <el-pagination
                    background
                    layout="prev, pager, next, total"
                    :current-page="filters.current"
                    :page-size="filters.pageSize"
                    :total="total"
                    @current-change="handlePageChange"
                />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchLaboratoryOptions, type SelectOption } from '../../api/modules/business'
import { downloadReportSummary, getReportSummary, type ReportRow } from '../../api/modules/report'

const loading = ref(false)
const rows = ref<ReportRow[]>([])
const total = ref(0)
const laboratoryOptions = ref<SelectOption[]>([])
const dateRange = ref<[string, string] | []>([])
const reportTypeMap: Record<string, string> = {
    borrow: '设备借用',
    repair: '设备维修',
    calibration: '设备校准',
    consumable: '耗材出库',
    hazardous: '危化品业务',
}

const filters = reactive({
    current: 1,
    pageSize: 10,
    labId: undefined as number | undefined,
    startDate: undefined as string | undefined,
    endDate: undefined as string | undefined,
    type: undefined as string | undefined,
})

async function loadData() {
    loading.value = true
    try {
        filters.startDate = dateRange.value[0]
        filters.endDate = dateRange.value[1]
        const result = await getReportSummary(filters)
        rows.value = result.data.records
        total.value = result.data.total
    } finally {
        loading.value = false
    }
}

async function loadOptions() {
    laboratoryOptions.value = await fetchLaboratoryOptions()
}

function handleFilterChange() {
    filters.current = 1
    loadData()
}

function resetFilters() {
    filters.current = 1
    filters.labId = undefined
    filters.startDate = undefined
    filters.endDate = undefined
    filters.type = undefined
    dateRange.value = []
    loadData()
}

function handlePageChange(page: number) {
    filters.current = page
    loadData()
}

function handleExport() {
    downloadReportSummary({
        labId: filters.labId,
        startDate: dateRange.value[0],
        endDate: dateRange.value[1],
        type: filters.type,
    })
        .then((blob) => {
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.download = '实验室业务汇总报表.csv'
            link.click()
            window.URL.revokeObjectURL(url)
        })
        .catch(() => ElMessage.error('导出失败'))
}

onMounted(async () => {
    await Promise.all([loadOptions(), loadData()])
})
</script>
