<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CrudPage from '../shared/CrudPage.vue'

const safetyLevelOptions = [
    { label: '一级', value: 1 },
    { label: '二级', value: 2 },
    { label: '三级', value: 3 },
]

const safetyLevelMap: Record<string, string> = {
    '1': '一级',
    '2': '二级',
    '3': '三级',
}

const config = computed(() => ({
    title: '实验室管理',
    subtitle: '维护实验室基础档案、地点、安全等级与启停状态。',
    endpoint: '/laboratories',
    createPermission: 'laboratory:edit',
    editPermission: 'laboratory:edit',
    deletePermission: 'laboratory:edit',
    createText: '新增实验室',
    displayMappings: {
        safetyLevel: safetyLevelMap,
    },
    columns: [
        { label: '实验室编码', prop: 'laboratoryCode' },
        { label: '实验室名称', prop: 'laboratoryName' },
        { label: '所在位置', prop: 'location' },
        { label: '联系电话', prop: 'contactPhone' },
        { label: '安全等级', prop: 'safetyLevel', type: 'select' as const, options: safetyLevelOptions },
    ],
    emptyRecord: () => ({
        laboratoryCode: '',
        laboratoryName: '',
        location: '',
        contactPhone: '',
        safetyLevel: 1,
        status: 1,
    }),
}))
</script>
