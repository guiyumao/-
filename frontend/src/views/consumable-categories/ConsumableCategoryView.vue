<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import CrudPage from '../shared/CrudPage.vue'

const config = {
    title: '耗材分类',
    subtitle: '维护试剂、过滤耗材、玻璃器皿与生物耗材等分类。',
    endpoint: '/consumable-categories',
    createPermission: 'consumable_category:edit',
    editPermission: 'consumable_category:edit',
    deletePermission: 'consumable_category:edit',
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
