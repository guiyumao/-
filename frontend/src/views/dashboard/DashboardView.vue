<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">仪表盘</h2>
                <p class="page-subtitle">实时汇总实验室资产规模、业务运行状态与近期提醒事项。</p>
            </div>
            <el-button @click="loadData">刷新</el-button>
        </div>

        <el-row :gutter="20">
            <el-col v-for="item in cards" :key="item.title" :xs="24" :sm="12" :lg="8" :xl="4">
                <el-card class="card-panel" shadow="never" body-style="padding: 26px">
                    <el-statistic :title="item.title" :value="item.value" :prefix="item.prefix" :suffix="item.suffix" />
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
            <el-col :xs="24" :lg="16">
                <el-card class="card-panel" shadow="never" body-style="padding: 24px">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
                        <div>
                            <h3 style="margin: 0">耗材消耗趋势</h3>
                            <p style="margin: 8px 0 0; color: var(--text-secondary)">近 6 日耗材出库金额趋势图</p>
                        </div>
                    </div>
                    <div ref="chartRef" style="height: 340px"></div>
                </el-card>
            </el-col>
            <el-col :xs="24" :lg="8">
                <el-card class="card-panel" shadow="never" body-style="padding: 24px; height: 100%">
                    <h3 style="margin: 0 0 12px">运营关注点</h3>
                    <el-timeline>
                        <el-timeline-item timestamp="到期提醒" placement="top">
                            校准即将到期 {{ stats?.pendingCalibrations ?? 0 }} 台，耗材即将过期 {{ stats?.expiringConsumables ?? 0 }} 种。
                        </el-timeline-item>
                        <el-timeline-item timestamp="设备借用" placement="top">
                            当前借出设备 {{ stats?.currentBorrowCount ?? 0 }} 台，建议优先关注逾期借用记录。
                        </el-timeline-item>
                        <el-timeline-item timestamp="耗材成本" placement="top">
                            本月耗材出库金额为 ¥{{ Number(stats?.monthlyConsumableAmount ?? 0).toFixed(2) }}。
                        </el-timeline-item>
                    </el-timeline>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { getDashboardStats, type DashboardStatsResponse } from '../../api/modules/dashboard'

const chartRef = ref<HTMLDivElement>()
const stats = ref<DashboardStatsResponse>()
let chart: echarts.ECharts | undefined

const cards = computed(() => [
    { title: '实验室数量', value: stats.value?.laboratoryCount ?? 0 },
    { title: '设备总数', value: stats.value?.equipmentCount ?? 0 },
    { title: '耗材种类', value: stats.value?.consumableCount ?? 0 },
    { title: '危化品种类', value: stats.value?.hazardousCount ?? 0 },
    { title: '当前借出', value: stats.value?.currentBorrowCount ?? 0 },
    { title: '校准待提醒', value: stats.value?.pendingCalibrations ?? 0 },
    { title: '耗材将过期', value: stats.value?.expiringConsumables ?? 0 },
    { title: '本月成本', value: Number(stats.value?.monthlyConsumableAmount ?? 0).toFixed(2), prefix: '¥' },
])

async function loadData() {
    const result = await getDashboardStats()
    stats.value = result.data
    await nextTick()
    renderChart()
}

function renderChart() {
    if (!chartRef.value || !stats.value) {
        return
    }
    chart ??= echarts.init(chartRef.value)
    chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 20, right: 20, top: 20, bottom: 20, containLabel: true },
        xAxis: {
            type: 'category',
            data: stats.value.consumableTrend.map((item) => item.label),
            axisLine: { lineStyle: { color: '#9aa8ad' } },
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            splitLine: { lineStyle: { color: 'rgba(28, 54, 66, 0.08)' } },
        },
        series: [
            {
                type: 'line',
                smooth: true,
                data: stats.value.consumableTrend.map((item) => item.value),
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(15, 139, 141, 0.35)' },
                        { offset: 1, color: 'rgba(15, 139, 141, 0.05)' },
                    ]),
                },
                lineStyle: { color: '#0f8b8d', width: 4 },
                itemStyle: { color: '#c8553d' },
            },
        ],
    })
}

function handleResize() {
    chart?.resize()
}

onMounted(async () => {
    await loadData()
    window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    chart?.dispose()
})
</script>
