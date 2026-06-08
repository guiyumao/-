<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">到期提醒</h2>
                <p class="page-subtitle">查看即将到期的设备校准和耗材批次。</p>
            </div>
            <el-button @click="loadData">刷新</el-button>
        </div>

        <el-card class="card-panel notification-panel" shadow="never">
            <template #header>
                <div style="font-weight: 700">我的通知</div>
            </template>
            <el-table v-loading="loading" :data="notifications">
                <el-table-column label="标题" min-width="150">
                    <template #default="{ row }">
                        <el-tag :type="row.readStatus === 0 ? 'danger' : 'info'" effect="light">
                            {{ row.title }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
                <el-table-column label="发送时间" min-width="160">
                    <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
                <el-card class="card-panel" shadow="never">
                    <template #header>
                        <div style="font-weight: 700">校准到期提醒</div>
                    </template>
                    <el-table v-loading="loading" :data="pendingCalibrations">
                        <el-table-column prop="itemName" label="设备名称" min-width="160" />
                        <el-table-column prop="laboratoryName" label="实验室" min-width="140" />
                        <el-table-column label="到期日期" min-width="160">
                            <template #default="{ row }">
                                <el-tag type="danger">{{ row.dueDate }}</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>

            <el-col :xs="24" :lg="12">
                <el-card class="card-panel" shadow="never">
                    <template #header>
                        <div style="font-weight: 700">耗材过期提醒</div>
                    </template>
                    <el-table v-loading="loading" :data="expiringConsumables">
                        <el-table-column prop="itemName" label="耗材名称" min-width="160" />
                        <el-table-column prop="batchNo" label="批号" min-width="120" />
                        <el-table-column prop="remainingQuantity" label="剩余数量" min-width="120" />
                        <el-table-column label="过期日期" min-width="160">
                            <template #default="{ row }">
                                <el-tag type="danger">{{ row.dueDate }}</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getMyNotifications, getReminders, type ReminderItem, type UserNotification } from '../../api/modules/reminder'
import { formatDateTime } from '../business/EquipmentBorrowBusinessHelpers'

const loading = ref(false)
const pendingCalibrations = ref<ReminderItem[]>([])
const expiringConsumables = ref<ReminderItem[]>([])
const notifications = ref<UserNotification[]>([])

async function loadData() {
    loading.value = true
    try {
        const [result, notificationResult] = await Promise.all([getReminders(), getMyNotifications()])
        pendingCalibrations.value = result.data.pendingCalibrations
        expiringConsumables.value = result.data.expiringConsumables
        notifications.value = notificationResult.data
    } finally {
        loading.value = false
    }
}

loadData()
</script>

<style scoped>
.notification-panel {
    margin-bottom: 20px;
}
</style>
