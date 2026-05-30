import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AppLayout from '../layout/AppLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import LoginView from '../views/login/LoginView.vue'
import LaboratoryView from '../views/laboratory/LaboratoryView.vue'
import UserView from '../views/users/UserView.vue'
import EquipmentCategoryView from '../views/equipment-categories/EquipmentCategoryView.vue'
import EquipmentView from '../views/equipment/EquipmentView.vue'
import ConsumableCategoryView from '../views/consumable-categories/ConsumableCategoryView.vue'
import ConsumableView from '../views/consumable/ConsumableView.vue'
import HazardousMaterialView from '../views/hazardous-materials/HazardousMaterialView.vue'
import EquipmentBorrowView from '../views/equipment-borrows/EquipmentBorrowView.vue'
import ConsumableInboundBusinessView from '../views/business/ConsumableInboundBusinessView.vue'
import ConsumableOutboundBusinessView from '../views/business/ConsumableOutboundBusinessView.vue'
import HazardousUsageBusinessView from '../views/business/HazardousUsageBusinessView.vue'
import EquipmentRepairView from '../views/business/EquipmentRepairView.vue'
import EquipmentCalibrationView from '../views/business/EquipmentCalibrationView.vue'
import ReportCenterView from '../views/reports/ReportCenterView.vue'
import RoleManagementView from '../views/roles/RoleManagementView.vue'
import InventoryAlertView from '../views/inventory/InventoryAlertView.vue'
import AuditLogView from '../views/audit/AuditLogView.vue'
import ReminderView from '../views/reminders/ReminderView.vue'
import { useAuthStore } from '../stores/auth'

const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        component: LoginView,
        meta: { title: '登录', public: true },
    },
    {
        path: '/',
        component: AppLayout,
        children: [
            { path: '', redirect: '/dashboard' },
            { path: '/dashboard', component: DashboardView, meta: { title: '仪表盘' } },
            { path: '/reminders', component: ReminderView, meta: { title: '到期提醒' } },
            { path: '/laboratories', component: LaboratoryView, meta: { title: '实验室管理' } },
            { path: '/users', component: UserView, meta: { title: '用户管理' } },
            { path: '/roles', component: RoleManagementView, meta: { title: '角色与菜单' } },
            { path: '/audit-logs', component: AuditLogView, meta: { title: '操作日志' } },
            { path: '/equipment-categories', component: EquipmentCategoryView, meta: { title: '设备分类' } },
            { path: '/equipment', component: EquipmentView, meta: { title: '设备台账' } },
            { path: '/equipment-borrows', component: EquipmentBorrowView, meta: { title: '设备借用' } },
            { path: '/equipment-repairs', component: EquipmentRepairView, meta: { title: '设备维修' } },
            { path: '/equipment-calibrations', component: EquipmentCalibrationView, meta: { title: '设备校准' } },
            { path: '/consumable-categories', component: ConsumableCategoryView, meta: { title: '耗材分类' } },
            { path: '/consumables', component: ConsumableView, meta: { title: '耗材台账' } },
            { path: '/consumable-inbounds', component: ConsumableInboundBusinessView, meta: { title: '耗材入库' } },
            { path: '/consumable-outbounds', component: ConsumableOutboundBusinessView, meta: { title: '耗材出库' } },
            { path: '/hazardous-materials', component: HazardousMaterialView, meta: { title: '危化品台账' } },
            { path: '/hazardous-usages', component: HazardousUsageBusinessView, meta: { title: '危化品领用' } },
            { path: '/inventory/alert', component: InventoryAlertView, meta: { title: '库存预警' } },
            { path: '/reports', component: ReportCenterView, meta: { title: '报表中心' } },
        ],
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    if (to.meta.public) {
        return true
    }
    if (!authStore.isAuthenticated) {
        return '/login'
    }
    if (!authStore.initialized || !authStore.user) {
        try {
            await authStore.bootstrap()
        } catch {
            return '/login'
        }
    }
    return true
})

export default router
