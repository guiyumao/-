<template>
    <div class="dashboard-shell">
        <section class="dashboard-hero card-panel">
            <div class="dashboard-hero-copy">
                <div class="eyebrow">Daily Overview</div>
                <h2 class="page-title">仪表盘</h2>
                <p class="page-subtitle">
                    实时查看实验室资产规模、流转状态与近期提醒，让今天优先处理的事项一眼可见。
                </p>
            </div>

            <div class="dashboard-hero-actions">
                <div class="hero-badge">
                    <span class="hero-badge-label">本月耗材成本</span>
                    <span class="hero-badge-value">¥{{ Number(stats?.monthlyConsumableAmount ?? 0).toFixed(2) }}</span>
                </div>
                <el-button @click="loadData">刷新数据</el-button>
            </div>
        </section>

        <section class="metric-grid">
            <article v-for="item in cards" :key="item.title" class="metric-card card-panel">
                <div class="metric-card-header">
                    <span>{{ item.title }}</span>
                    <span class="metric-card-dot"></span>
                </div>
                <div class="metric-value metric-card-value">
                    {{ item.prefix || '' }}{{ item.value }}{{ item.suffix || '' }}
                </div>
                <p class="metric-card-note">{{ item.note }}</p>
            </article>
        </section>

        <section class="dashboard-content">
            <article class="chart-panel card-panel">
                <div class="chart-panel-head">
                    <div>
                        <div class="eyebrow">Consumption Trend</div>
                        <h3>耗材消耗走势</h3>
                        <p>近 6 个月耗材出库金额趋势图，用于观察阶段性消耗节奏。</p>
                    </div>
                </div>
                <div ref="chartRef" class="chart-stage"></div>
            </article>

            <article class="focus-panel card-panel">
                <div class="eyebrow">Operation Focus</div>
                <h3>运营关注点</h3>
                <div class="focus-list">
                    <div v-for="item in focusItems" :key="item.title" class="focus-item">
                        <div class="focus-item-title">{{ item.title }}</div>
                        <div class="focus-item-value">{{ item.value }}</div>
                        <p>{{ item.description }}</p>
                    </div>
                </div>
            </article>
        </section>
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
    { title: '实验室数量', value: stats.value?.laboratoryCount ?? 0, note: '基础空间与组织单元' },
    { title: '设备总数', value: stats.value?.equipmentCount ?? 0, note: '纳入台账的设备资产' },
    { title: '耗材种类', value: stats.value?.consumableCount ?? 0, note: '当前维护的耗材条目' },
    { title: '危化品种类', value: stats.value?.hazardousCount ?? 0, note: '特殊物资与安全记录' },
    { title: '当前借出', value: stats.value?.currentBorrowCount ?? 0, note: '仍处于借用流程中的设备' },
    { title: '待校准项', value: stats.value?.pendingCalibrations ?? 0, note: '近期需要优先跟进' },
    { title: '临期耗材', value: stats.value?.expiringConsumables ?? 0, note: '需要尽快复核的库存' },
    { title: '本月成本', value: Number(stats.value?.monthlyConsumableAmount ?? 0).toFixed(2), prefix: '¥', note: '耗材出库金额累计' },
])

const focusItems = computed(() => [
    {
        title: '到期提醒',
        value: `${stats.value?.pendingCalibrations ?? 0} 项校准 / ${stats.value?.expiringConsumables ?? 0} 项临期`,
        description: '优先核对即将到期的设备校准和耗材保质期，避免日常操作被动中断。',
    },
    {
        title: '借用状态',
        value: `${stats.value?.currentBorrowCount ?? 0} 台设备在借`,
        description: '建议优先处理接近归还期限的设备，减少设备占用时间过长的情况。',
    },
    {
        title: '消耗强度',
        value: `¥${Number(stats.value?.monthlyConsumableAmount ?? 0).toFixed(2)}`,
        description: '将本月耗材支出与出库频次结合起来看，更容易发现异常波动。',
    },
]
)

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
        grid: { left: 16, right: 16, top: 24, bottom: 12, containLabel: true },
        xAxis: {
            type: 'category',
            data: stats.value.consumableTrend.map((item) => item.label),
            axisLine: { lineStyle: { color: '#8aa1a8' } },
            axisTick: { show: false },
            axisLabel: { color: '#5f7581' },
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: '#5f7581' },
            splitLine: { lineStyle: { color: 'rgba(21, 49, 59, 0.08)' } },
        },
        series: [
            {
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 10,
                data: stats.value.consumableTrend.map((item) => item.value),
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(18, 127, 114, 0.32)' },
                        { offset: 1, color: 'rgba(18, 127, 114, 0.04)' },
                    ]),
                },
                lineStyle: { color: '#127f72', width: 4 },
                itemStyle: {
                    color: '#bf5d3d',
                    borderColor: '#fff8f1',
                    borderWidth: 2,
                },
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

<style scoped>
.dashboard-shell {
    display: grid;
    gap: 20px;
}

.dashboard-hero {
    padding: 24px 26px;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 24px;
}

.dashboard-hero-actions {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.hero-badge {
    display: grid;
    gap: 6px;
    padding: 14px 16px;
    min-width: 220px;
    border-radius: 20px;
    background: linear-gradient(135deg, rgba(18, 127, 114, 0.12), rgba(212, 161, 76, 0.14));
    border: 1px solid rgba(18, 127, 114, 0.12);
}

.hero-badge-label {
    color: var(--text-secondary);
    font-size: 13px;
}

.hero-badge-value {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
}

.metric-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
}

.metric-card {
    padding: 22px;
}

.metric-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    color: var(--text-secondary);
    font-size: 13px;
}

.metric-card-dot {
    width: 10px;
    height: 10px;
    border-radius: 999px;
    background: linear-gradient(135deg, var(--accent), var(--accent-gold));
}

.metric-card-value {
    margin-top: 18px;
    font-size: 34px;
}

.metric-card-note {
    margin: 12px 0 0;
    color: var(--text-secondary);
    line-height: 1.6;
}

.dashboard-content {
    display: grid;
    grid-template-columns: minmax(0, 1.6fr) minmax(300px, 0.9fr);
    gap: 20px;
}

.chart-panel,
.focus-panel {
    padding: 24px;
}

.chart-panel-head h3,
.focus-panel h3 {
    margin: 12px 0 0;
    font-family: var(--font-display);
    font-size: 24px;
}

.chart-panel-head p {
    margin: 10px 0 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

.chart-stage {
    height: 360px;
    margin-top: 20px;
}

.focus-list {
    display: grid;
    gap: 14px;
    margin-top: 18px;
}

.focus-item {
    padding: 18px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.58);
    border: 1px solid var(--line-soft);
}

.focus-item-title {
    font-size: 13px;
    color: var(--text-soft);
}

.focus-item-value {
    margin-top: 8px;
    font-family: var(--font-display);
    font-size: 22px;
    font-weight: 700;
}

.focus-item p {
    margin: 10px 0 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

@media (max-width: 1180px) {
    .metric-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .dashboard-content {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 720px) {
    .dashboard-hero {
        padding: 20px;
        flex-direction: column;
        align-items: stretch;
    }

    .metric-grid {
        grid-template-columns: 1fr;
    }

    .chart-panel,
    .focus-panel {
        padding: 20px;
    }
}
</style>
