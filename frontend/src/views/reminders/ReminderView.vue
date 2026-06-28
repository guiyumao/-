<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">协同提醒中心</h2>
                <p class="page-subtitle">不只展示到期数据，还告诉我们是谁该处理、来源于哪条业务链、会影响哪个角色的下一步动作。</p>
            </div>
            <el-button @click="loadData">刷新</el-button>
        </div>

        <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">未读通知</div>
                    <div class="metric-value">{{ unreadCount }}</div>
                    <div class="metric-note">优先处理需要立即响应的角色消息</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">校准待跟进</div>
                    <div class="metric-value warning">{{ pendingCalibrations.length }}</div>
                    <div class="metric-note">关联设备可用性和借用权限</div>
                </el-card>
            </el-col>
            <el-col :xs="24" :md="8">
                <el-card class="card-panel metric-card" shadow="never">
                    <div class="metric-label">耗材到期提醒</div>
                    <div class="metric-value">{{ expiringConsumables.length }}</div>
                    <div class="metric-note">需要库存角色尽快核查批次去向</div>
                </el-card>
            </el-col>
        </el-row>

        <el-card class="card-panel notification-panel" shadow="never">
            <template #header>
                <div class="section-header">
                    <div>
                        <div class="section-title">我的通知</div>
                        <p class="section-desc">通知会把借用催还、跨角色交接和来源链路一起记录下来。</p>
                    </div>
                </div>
            </template>

            <div class="notification-list">
                <el-card v-for="item in notifications" :key="item.id" class="notify-card" shadow="never">
                    <div class="notify-head">
                        <div class="notify-title-row">
                            <el-tag :type="item.readStatus === 0 ? 'danger' : 'info'" effect="light">
                                {{ item.readStatus === 0 ? '未读' : '已读' }}
                            </el-tag>
                            <strong>{{ item.title }}</strong>
                        </div>
                        <span class="notify-time">{{ formatDateTime(item.createTime) }}</span>
                    </div>
                    <p class="notify-content">{{ item.content }}</p>
                    <div class="notify-meta">
                        <span>来源类型：{{ notificationTypeText(item.notificationType) }}</span>
                        <span>关联业务：{{ relatedTypeText(item.relatedType) }}</span>
                        <span>发送人：{{ userName(item.senderUserId) }}</span>
                    </div>
                </el-card>
                <el-empty v-if="!notifications.length && !loading" description="当前没有通知" />
            </div>
        </el-card>

        <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
                <el-card class="card-panel" shadow="never">
                    <template #header>
                        <div class="section-header">
                            <div>
                                <div class="section-title">校准协同待办</div>
                                <p class="section-desc">来源于设备台账周期、维修后复检或到期提醒，需由相关角色完成校准确认。</p>
                            </div>
                        </div>
                    </template>

                    <div class="todo-list">
                        <el-card v-for="item in pendingCalibrations" :key="`${item.itemName}-${item.dueDate}`" class="todo-card" shadow="never">
                            <div class="todo-head">
                                <strong>{{ item.itemName }}</strong>
                                <el-tag type="warning" effect="light">校准待跟进</el-tag>
                            </div>
                            <p>实验室：{{ item.laboratoryName }}</p>
                            <p>到期时间：{{ formatDateTime(item.dueDate) }}</p>
                            <p>下一步责任：校准角色确认结果，实验室管理员同步借用策略。</p>
                            <p>来源依据：设备下次校准日期已进入提醒窗口。</p>
                        </el-card>
                        <el-empty v-if="!pendingCalibrations.length && !loading" description="当前没有校准待办" />
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :lg="12">
                <el-card class="card-panel" shadow="never">
                    <template #header>
                        <div class="section-header">
                            <div>
                                <div class="section-title">耗材批次待办</div>
                                <p class="section-desc">提醒库存或耗材角色核查即将到期批次，必要时通知申请人优先消耗或停止发放。</p>
                            </div>
                        </div>
                    </template>

                    <div class="todo-list">
                        <el-card v-for="item in expiringConsumables" :key="`${item.itemName}-${item.batchNo}-${item.dueDate}`" class="todo-card" shadow="never">
                            <div class="todo-head">
                                <strong>{{ item.itemName }}</strong>
                                <el-tag type="danger" effect="light">临期批次</el-tag>
                            </div>
                            <p>实验室：{{ item.laboratoryName }}</p>
                            <p>批号：{{ item.batchNo || '-' }}</p>
                            <p>剩余数量：{{ item.remainingQuantity ?? '-' }}</p>
                            <p>到期时间：{{ formatDateTime(item.dueDate) }}</p>
                            <p>来源依据：库存批次有效期进入提醒窗口，需要关联出库申请一起判断处理。</p>
                        </el-card>
                        <el-empty v-if="!expiringConsumables.length && !loading" description="当前没有耗材待办" />
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getMyNotifications, getReminders, type ReminderItem, type UserNotification } from '../../api/modules/reminder'
import { fetchUserOptions, optionsToMap } from '../../api/modules/business'
import { formatDateTime } from '../business/EquipmentBorrowBusinessHelpers'

const loading = ref(false)
const pendingCalibrations = ref<ReminderItem[]>([])
const expiringConsumables = ref<ReminderItem[]>([])
const notifications = ref<UserNotification[]>([])
const userMap = ref<Record<string, string>>({})

const unreadCount = computed(() => notifications.value.filter((item) => item.readStatus === 0).length)

function userName(userId?: number) {
    if (!userId) {
        return '系统自动发送'
    }
    return userMap.value[String(userId)] || `用户 ${userId}`
}

function notificationTypeText(type?: string) {
    if (type === 'equipment_overdue') {
        return '设备催还'
    }
    return type || '系统通知'
}

function relatedTypeText(type?: string) {
    if (type === 'equipment_borrow') {
        return '设备借用'
    }
    return type || '未关联'
}

async function loadData() {
    loading.value = true
    try {
        const [result, notificationResult, users] = await Promise.all([
            getReminders(),
            getMyNotifications(),
            fetchUserOptions(),
        ])
        pendingCalibrations.value = result.data.pendingCalibrations
        expiringConsumables.value = result.data.expiringConsumables
        notifications.value = notificationResult.data
        userMap.value = optionsToMap(users)
    } finally {
        loading.value = false
    }
}

loadData()
</script>

<style scoped>
.stats-row {
    margin-bottom: 20px;
}

.metric-card {
    border-radius: 20px;
    background:
        radial-gradient(circle at top right, rgba(255, 176, 107, 0.18), transparent 40%),
        linear-gradient(160deg, rgba(255, 255, 255, 0.98), rgba(249, 250, 245, 0.98));
}

.metric-label {
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 700;
}

.metric-value {
    margin-top: 10px;
    color: var(--text-main);
    font-family: var(--font-display);
    font-size: 34px;
    font-weight: 800;
}

.metric-value.warning {
    color: #946328;
}

.metric-note {
    margin-top: 8px;
    color: var(--text-secondary);
    font-size: 13px;
}

.notification-panel {
    margin-bottom: 20px;
}

.section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
}

.section-title {
    color: var(--text-main);
    font-weight: 800;
}

.section-desc,
.notify-content,
.todo-card p {
    margin: 6px 0 0;
    color: var(--text-secondary);
    line-height: 1.68;
}

.notification-list,
.todo-list {
    display: grid;
    gap: 14px;
}

.notify-card,
.todo-card {
    border-radius: 20px;
    border: 1px solid rgba(33, 55, 61, 0.1);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(249, 246, 240, 0.98));
    box-shadow: 0 14px 32px rgba(24, 44, 50, 0.07);
}

.notify-head,
.todo-head,
.notify-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
}

.notify-title-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

.notify-title-row strong,
.todo-head strong {
    color: var(--text-main);
}

.notify-time {
    color: var(--text-secondary);
    font-size: 12px;
}

.notify-meta {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed rgba(33, 55, 61, 0.12);
    color: var(--text-secondary);
    font-size: 12px;
    flex-wrap: wrap;
}

@media (max-width: 1100px) {
    .notify-head,
    .todo-head,
    .notify-meta,
    .section-header {
        flex-direction: column;
        align-items: flex-start;
    }
}
</style>
