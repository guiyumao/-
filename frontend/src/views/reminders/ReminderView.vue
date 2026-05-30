<template>
    <div>
        <div class="page-header">
            <div>
                <h2 class="page-title">到期提醒</h2>
                <p class="page-subtitle">查看即将到期的设备校准和耗材批次。</p>
            </div>
            <el-button @click="loadData">刷新</el-button>
        </div>

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
import { getReminders, type ReminderItem } from '../../api/modules/reminder'

const loading = ref(false)
const pendingCalibrations = ref<ReminderItem[]>([])
const expiringConsumables = ref<ReminderItem[]>([])

async function loadData() {
    loading.value = true
    try {
        const result = await getReminders()
        pendingCalibrations.value = result.data.pendingCalibrations
        expiringConsumables.value = result.data.expiringConsumables
    } finally {
        loading.value = false
    }
}

loadData()
</script>
