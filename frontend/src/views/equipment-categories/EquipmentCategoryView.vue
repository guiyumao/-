<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import CrudPage from '../shared/CrudPage.vue'

const config = {
    title: '设备分类',
    subtitle: '维护分析仪器、精密仪器与通用设备等分类。',
    endpoint: '/equipment-categories',
    createPermission: 'equipment_category:edit',
    editPermission: 'equipment_category:edit',
    deletePermission: 'equipment_category:edit',
    createText: '新增分类',
    columns: [
        { label: '分类编码', prop: 'categoryCode' },
        { label: '分类名称', prop: 'categoryName' },
        { label: '分类说明', prop: 'description', type: 'textarea' as const },
    ],
    emptyRecord: () => ({
        categoryCode: '',
        categoryName: '',
        description: '',
        status: 1,
    }),
}
</script>
