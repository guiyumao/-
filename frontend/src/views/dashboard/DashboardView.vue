<template>
    <div class="dashboard-shell">
        <section class="dashboard-hero card-panel">
            <div class="dashboard-hero-copy">
                <div class="eyebrow">数据总览</div>
                <h2 class="page-title">仪表盘</h2>
                <p class="page-subtitle">
                    实时查看实验室资源规模、流转状态和待处理事项，把当天最重要的内容先放到眼前。
                </p>
            </div>

            <div class="dashboard-hero-actions">
                <div class="hero-badge">
                    <span class="hero-badge-label">本月耗材金额</span>
                    <span class="hero-badge-value">¥{{ monthlyAmount }}</span>
                </div>
                <el-button :loading="loading" @click="loadData">刷新数据</el-button>
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
                        <div class="eyebrow">趋势变化</div>
                        <h3>耗材支出趋势</h3>
                        <p>按月观察耗材出库金额变化，帮助判断是否存在集中消耗或异常波动。</p>
                    </div>
                </div>
                <div ref="chartRef" class="chart-stage"></div>
            </article>

            <article class="focus-panel card-panel">
                <div class="eyebrow">当前重点</div>
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
const loading = ref(false)
let chart: echarts.ECharts | undefined

const monthlyAmount = computed(() => Number(stats.value?.monthlyConsumableAmount ?? 0).toFixed(2))

const cards = computed(() => [
    { title: '实验室数量', value: stats.value?.laboratoryCount ?? 0, note: '当前已接入的实验室总数。' },
    { title: '设备总数', value: stats.value?.equipmentCount ?? 0, note: '纳入台账管理的设备数量。' },
    { title: '耗材种类', value: stats.value?.consumableCount ?? 0, note: '正在维护的耗材分类和台账。' },
    { title: '危化品种类', value: stats.value?.hazardousCount ?? 0, note: '特殊物资与安全管理对象。' },
    { title: '当前借出', value: stats.value?.currentBorrowCount ?? 0, note: '仍处于借用流程中的设备。' },
    { title: '待校准', value: stats.value?.pendingCalibrations ?? 0, note: '需要优先处理的校准任务。' },
    { title: '临期耗材', value: stats.value?.expiringConsumables ?? 0, note: '需要尽快复核的库存项目。' },
    { title: '本月支出', value: monthlyAmount.value, prefix: '¥', note: '耗材出库金额累计。' },
])

const focusItems = computed(() => [
    {
        title: '待办提醒',
        value: `${stats.value?.pendingCalibrations ?? 0} 项校准 / ${stats.value?.expiringConsumables ?? 0} 项临期`,
        description: '优先处理到期和临期事项，避免影响日常业务。',
    },
    {
        title: '借用状态',
        value: `${stats.value?.currentBorrowCount ?? 0} 台设备在借`,
        description: '及时跟进即将归还的设备，减少占用时间过长。',
    },
    {
        title: '耗材支出',
        value: `¥${monthlyAmount.value}`,
        description: '把本月耗材支出和出库趋势放在一起看，更容易发现异常。',
    },
])

async function loadData() {
    loading.value = true
    try {
        const result = await getDashboardStats()
        stats.value = result.data
        await nextTick()
        renderChart()
    } finally {
        loading.value = false
    }
}

function renderChart() {
    if (!chartRef.value || !stats.value) {
        return
    }

    chart ??= echarts.init(chartRef.value)
    chart.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis' },
        grid: { left: 16, right: 18, top: 24, bottom: 12, containLabel: true },
        xAxis: {
            type: 'category',
            data: stats.value.consumableTrend.map((item) => item.label),
            axisLine: { lineStyle: { color: '#5d7380' } },
            axisTick: { show: false },
            axisLabel: { color: '#9bb0bc' },
        },
        yAxis: {
            type: 'value',
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: '#9bb0bc' },
            splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
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
                        { offset: 0, color: 'rgba(86, 200, 182, 0.34)' },
                        { offset: 1, color: 'rgba(86, 200, 182, 0.04)' },
                    ]),
                },
                lineStyle: { color: '#56c8b6', width: 4 },
                itemStyle: {
                    color: '#f0be67',
                    borderColor: '#0d1721',
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
    background: linear-gradient(135deg, rgba(86, 200, 182, 0.14), rgba(240, 190, 103, 0.14));
    border: 1px solid rgba(255, 255, 255, 0.08);
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
    background: rgba(255, 255, 255, 0.04);
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
