<template>
    <el-container class="page-shell app-layout">
        <el-aside class="app-sidebar">
            <div class="sidebar-brand">
                <div class="eyebrow">Lab Operations Console</div>
                <h1>实验室设备与耗材平台</h1>
                <p>把设备、耗材、危化品、提醒与报表汇到同一个清晰的操作中枢。</p>
            </div>

            <div class="sidebar-summary">
                <div class="summary-card">
                    <div class="summary-label">当前状态</div>
                    <div class="summary-value">稳定在线</div>
                    <div class="summary-note">数据、业务、审计三条主线集中管理</div>
                </div>
                <div class="summary-strip">
                    <span class="soft-pill">多角色协作</span>
                    <span class="soft-pill">流程可追踪</span>
                </div>
            </div>

            <el-scrollbar class="sidebar-scroll">
                <el-menu
                    :default-active="route.path"
                    class="sidebar-menu"
                    background-color="transparent"
                    text-color="rgba(248, 243, 236, 0.72)"
                    active-text-color="#fff8f0"
                    unique-opened
                    @select="handleSelect"
                >
                    <template v-for="item in menus" :key="item.id">
                        <el-sub-menu v-if="item.children?.length" :index="item.path || String(item.id)">
                            <template #title>
                                <span>{{ item.label }}</span>
                            </template>
                            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                                {{ child.label }}
                            </el-menu-item>
                        </el-sub-menu>

                        <el-menu-item v-else :index="item.path">
                            {{ item.label }}
                        </el-menu-item>
                    </template>
                </el-menu>
            </el-scrollbar>

            <div class="sidebar-footer">
                <div class="footer-label">今日面板</div>
                <div class="footer-value">{{ todayLabel }}</div>
            </div>
        </el-aside>

        <el-container class="app-main-shell">
            <el-header class="app-header">
                <div class="card-panel app-header-card">
                    <div class="module-copy">
                        <div class="eyebrow">Current Module</div>
                        <div class="module-title-row">
                            <h2>{{ currentTitle }}</h2>
                            <span class="module-chip">{{ currentPath }}</span>
                        </div>
                        <p>{{ currentDescription }}</p>
                    </div>

                    <div class="header-actions">
                        <div class="header-pills">
                            <span class="header-pill is-online">在线协同</span>
                            <span class="header-pill">{{ userRoleLabel }}</span>
                        </div>

                        <div class="user-card">
                            <div class="user-card-copy">
                                <div class="user-name">{{ authStore.user?.realName || '访客' }}</div>
                                <div class="user-meta">{{ authStore.user?.username || '-' }}</div>
                            </div>
                            <el-button round @click="handleLogout">退出登录</el-button>
                        </div>
                    </div>
                </div>
            </el-header>

            <el-main class="app-main">
                <RouterView />
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const menus = computed(() => authStore.sidebarMenus)

const moduleDescriptions: Record<string, string> = {
    '/dashboard': '快速查看实验室资产规模、业务运行状态与近期提醒，让每天的工作从重点开始。',
    '/reminders': '统一查看到期、预警与待处理事项，减少设备与耗材管理中的遗漏。',
    '/laboratories': '维护实验室台账与基础信息，为用户、资产与权限提供归属上下文。',
    '/users': '集中管理人员账号、实验室归属与角色能力，让协作边界更清楚。',
    '/roles': '调整角色菜单和操作权限，控制不同岗位的访问范围与动作能力。',
    '/audit-logs': '按时间线追踪关键操作，让每一次修改、借还与维护都有迹可循。',
    '/equipment-categories': '梳理设备分类结构，统一资产命名与统计口径。',
    '/equipment': '查看设备台账、状态与归属，支撑借用、维修与校准等后续流程。',
    '/equipment-borrows': '跟踪设备借用、归还与逾期状态，提升流转效率。',
    '/equipment-repairs': '记录维修过程与结果，帮助维护设备可用性与维修历史。',
    '/equipment-calibrations': '集中处理校准计划与到期提醒，降低关键设备失准风险。',
    '/consumable-categories': '统一耗材分类，为采购、出入库和报表分析提供基础。',
    '/consumables': '管理耗材基础台账、库存信息与保质期状态。',
    '/consumable-inbounds': '登记采购入库与补货动作，让库存变化来源清晰可查。',
    '/consumable-outbounds': '记录领用与出库过程，快速掌握成本去向与消耗节奏。',
    '/hazardous-materials': '维护危化品台账和属性信息，强化特殊物资管理。',
    '/hazardous-usages': '追踪危化品领用、归还与处理动作，兼顾安全与合规。',
    '/inventory/alert': '聚焦库存预警、短缺与临期项目，方便优先处理高风险项。',
    '/reports': '按业务维度查看统计结果与导出报表，给管理决策提供依据。',
}

const currentTitle = computed(() => String(route.meta.title || '仪表盘'))
const currentDescription = computed(() => moduleDescriptions[route.path] || '在统一的后台界面中完成日常操作、追踪状态并保持数据有序。')
const currentPath = computed(() => route.path === '/' ? '/dashboard' : route.path)
const userRoleLabel = computed(() => authStore.user?.roleCodes?.[0] || '系统用户')
const todayLabel = new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'long',
}).format(new Date())

function handleSelect(index: string) {
    if (index.startsWith('/')) {
        router.push(index)
    }
}

function handleLogout() {
    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
}
</script>

<style scoped>
.app-layout {
    gap: 18px;
    padding: 18px;
}

.app-sidebar {
    width: 320px;
    padding: 28px 20px 20px;
    border-radius: var(--radius-xl);
    background: var(--bg-sidebar-glow), var(--bg-sidebar);
    color: #f8f3ec;
    box-shadow: 0 24px 56px rgba(10, 24, 30, 0.24);
    border: 1px solid rgba(255, 255, 255, 0.08);
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.sidebar-brand h1 {
    margin: 12px 0 0;
    font-family: var(--font-display);
    font-size: 32px;
    line-height: 1.08;
}

.sidebar-brand p {
    margin: 14px 0 0;
    color: rgba(248, 243, 236, 0.72);
    line-height: 1.7;
}

.sidebar-summary {
    display: grid;
    gap: 14px;
}

.summary-card {
    padding: 18px;
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(14px);
}

.summary-label,
.footer-label {
    font-size: 12px;
    letter-spacing: 0.16em;
    text-transform: uppercase;
    color: rgba(248, 243, 236, 0.58);
}

.summary-value,
.footer-value {
    margin-top: 10px;
    font-family: var(--font-display);
    font-size: 24px;
    font-weight: 700;
}

.summary-note {
    margin-top: 8px;
    color: rgba(248, 243, 236, 0.72);
    line-height: 1.6;
}

.summary-strip {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.sidebar-scroll {
    flex: 1;
    min-height: 0;
}

.sidebar-footer {
    padding: 16px 18px;
    border-radius: 20px;
    background: rgba(7, 18, 23, 0.22);
    border: 1px solid rgba(255, 255, 255, 0.08);
}

.app-main-shell {
    min-width: 0;
}

.app-header {
    height: auto;
    padding: 0;
}

.app-header-card {
    padding: 22px 26px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
}

.module-copy {
    min-width: 0;
}

.module-title-row {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.module-title-row h2 {
    margin: 10px 0 0;
    font-family: var(--font-display);
    font-size: 28px;
    line-height: 1.1;
}

.module-title-row p {
    margin: 0;
}

.module-copy p {
    margin: 12px 0 0;
    max-width: 760px;
    color: var(--text-secondary);
    line-height: 1.7;
}

.module-chip {
    display: inline-flex;
    align-items: center;
    padding: 8px 12px;
    border-radius: 999px;
    background: rgba(18, 127, 114, 0.1);
    color: var(--accent-deep);
    font-size: 12px;
    font-weight: 700;
}

.header-actions {
    display: grid;
    gap: 14px;
    justify-items: end;
}

.header-pills {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px;
}

.header-pill {
    display: inline-flex;
    align-items: center;
    padding: 10px 14px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.72);
    border: 1px solid var(--line-soft);
    color: var(--text-main);
    font-size: 13px;
    font-weight: 600;
}

.header-pill.is-online {
    color: var(--success);
}

.user-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 12px 14px 12px 16px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.74);
    border: 1px solid var(--line-soft);
}

.user-card-copy {
    text-align: right;
}

.user-name {
    font-weight: 700;
}

.user-meta {
    margin-top: 4px;
    color: var(--text-secondary);
    font-size: 13px;
}

.app-main {
    padding: 18px 0 0;
}

:deep(.sidebar-menu .el-menu-item),
:deep(.sidebar-menu .el-sub-menu__title) {
    height: 48px;
    margin-bottom: 8px;
    padding-inline: 16px;
}

:deep(.sidebar-menu .el-sub-menu .el-menu-item) {
    height: 44px;
    margin-left: 8px;
    margin-bottom: 6px;
    border-radius: 14px;
}

:deep(.sidebar-menu .el-menu-item.is-active),
:deep(.sidebar-menu .el-sub-menu__title:hover),
:deep(.sidebar-menu .el-menu-item:hover) {
    background: rgba(255, 255, 255, 0.12);
}

@media (max-width: 1080px) {
    .app-layout {
        display: block;
    }

    .app-sidebar {
        width: 100%;
        margin-bottom: 18px;
    }

    .app-header-card {
        flex-direction: column;
        align-items: stretch;
    }

    .header-actions,
    .header-pills {
        justify-items: stretch;
        justify-content: flex-start;
    }

    .user-card {
        justify-content: space-between;
    }
}

@media (max-width: 720px) {
    .app-layout {
        padding: 12px;
    }

    .app-sidebar {
        padding: 22px 16px 16px;
        border-radius: 24px;
    }

    .module-title-row h2 {
        font-size: 24px;
    }

    .user-card {
        flex-direction: column;
        align-items: stretch;
    }

    .user-card-copy {
        text-align: left;
    }
}
</style>
