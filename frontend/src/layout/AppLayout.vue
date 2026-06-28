<template>
    <el-container class="page-shell app-layout">
        <el-aside class="app-sidebar">
            <div class="sidebar-brand">
                <div class="eyebrow">实验室运营中枢</div>
                <h1>实验室设备与耗材管理系统</h1>
                <p>设备、耗材、危化品、提醒和报表都放在同一处，减少切换和重复录入。</p>
            </div>

            <div class="sidebar-summary">
                <div class="summary-card">
                    <div class="summary-label">当前状态</div>
                    <div class="summary-value">在线运行</div>
                    <div class="summary-note">读取真实后端菜单与权限，不再依赖前端假数据。</div>
                </div>
                <div class="summary-strip">
                    <span class="soft-pill">权限驱动</span>
                    <span class="soft-pill">中文回退</span>
                </div>
            </div>

            <el-scrollbar class="sidebar-scroll">
                <el-menu
                    :default-active="route.path"
                    class="sidebar-menu"
                    background-color="transparent"
                    text-color="rgba(238, 244, 248, 0.74)"
                    active-text-color="#ffffff"
                    unique-opened
                    @select="handleSelect"
                >
                    <template v-for="item in menus" :key="item.id">
                        <el-sub-menu v-if="item.children?.length" :index="item.path || String(item.id)">
                            <template #title>
                                <span>{{ menuLabel(item) }}</span>
                            </template>
                            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                                {{ menuLabel(child) }}
                            </el-menu-item>
                        </el-sub-menu>
                        <el-menu-item v-else :index="item.path">
                            {{ menuLabel(item) }}
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
                        <div class="eyebrow">当前模块</div>
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
import { roleLabels } from '../constants/roles'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menus = computed(() => authStore.sidebarMenus)
const menuLabelMap: Record<string, string> = {
    '/dashboard': '仪表盘',
    '/reminders': '到期提醒',
    '/laboratories': '实验室管理',
    '/users': '用户管理',
    '/roles': '角色与菜单',
    '/audit-logs': '操作日志',
    '/equipment-categories': '设备分类',
    '/equipment': '设备台账',
    '/equipment-borrows': '设备借用',
    '/equipment-repairs': '设备维修',
    '/equipment-calibrations': '设备校准',
    '/consumable-categories': '耗材分类',
    '/consumables': '耗材台账',
    '/consumable-inbounds': '耗材入库',
    '/consumable-outbounds': '耗材出库',
    '/hazardous-materials': '危化品台账',
    '/hazardous-usages': '危化品领用',
    '/inventory/alert': '库存预警',
    '/reports': '报表中心',
    '/system': '系统管理',
    '/assets': '资产档案',
    '/business': '业务流程',
    '/inventory': '库存管理',
}

const moduleDescriptions: Record<string, string> = {
    '/dashboard': '查看关键统计、趋势和待办，快速把握实验室当天重点。',
    '/reminders': '集中查看到期、预警和待处理事项，减少遗漏。',
    '/laboratories': '维护实验室基础信息，为人员和资产提供归属关系。',
    '/users': '管理账号、角色和实验室归属，统一权限边界。',
    '/roles': '配置角色与菜单授权，控制不同岗位可见范围。',
    '/audit-logs': '按时间追踪关键操作，便于审计和回溯。',
    '/equipment-categories': '整理设备分类，统一命名和统计口径。',
    '/equipment': '管理设备台账、状态和归属，支撑借用与维修流程。',
    '/equipment-borrows': '跟踪设备借用、归还和逾期情况。',
    '/equipment-repairs': '记录维修过程和结果，保留完整维修历史。',
    '/equipment-calibrations': '集中处理校准计划和到期提醒。',
    '/consumable-categories': '统一耗材分类，方便采购与库存分析。',
    '/consumables': '管理耗材台账、库存和保质期状态。',
    '/consumable-inbounds': '登记采购入库和补货动作。',
    '/consumable-outbounds': '记录领用和出库过程。',
    '/hazardous-materials': '维护危化品台账和属性信息。',
    '/hazardous-usages': '跟踪危化品领用、归还和处置动作。',
    '/inventory/alert': '聚焦库存预警、短缺和临期项目。',
    '/reports': '按业务维度查看统计结果和报表导出。',
}

const currentPath = computed(() => (route.path === '/' ? '/dashboard' : route.path))
const currentTitle = computed(() => String(route.meta.title || '仪表盘'))
const currentDescription = computed(() => moduleDescriptions[currentPath.value] || '在统一的后台界面中完成日常操作、跟踪状态并保持数据有序。')
const userRoleLabel = computed(() => {
    const roleCode = authStore.activeRoleCode || authStore.user?.roleCodes?.[0]
    return roleCode ? roleLabels[roleCode] || roleCode : '系统用户'
})
const todayLabel = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
}).format(new Date())

function isBadLabel(label?: string) {
    return !label || label === '???' || /^[?\s]+$/.test(label) || label.startsWith('/')
}

function menuLabel(item: { label?: string; path?: string }) {
    if (!isBadLabel(item.label)) {
        return item.label
    }
    return menuLabelMap[item.path || ''] || item.path || '未命名菜单'
}

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
    width: 330px;
    padding: 28px 20px 20px;
    border-radius: var(--radius-xl);
    background: var(--bg-sidebar-glow), var(--bg-sidebar);
    color: #f4f8fb;
    box-shadow: 0 24px 56px rgba(4, 9, 16, 0.34);
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
    color: rgba(238, 244, 248, 0.74);
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
    color: rgba(238, 244, 248, 0.58);
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
    color: rgba(238, 244, 248, 0.74);
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
    background: rgba(7, 18, 23, 0.28);
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
    background: rgba(86, 200, 182, 0.12);
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
    background: rgba(255, 255, 255, 0.04);
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
    background: rgba(255, 255, 255, 0.05);
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
