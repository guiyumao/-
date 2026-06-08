<template>
    <CategoryCardPage :config="config" />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import CategoryCardPage from '../shared/CategoryCardPage.vue'
import { useConsumableCrudConfig } from '../consumable/useConsumableCrudConfig'
import { useCrudSync } from '../../utils/crudSync'

const { config: consumableConfig, loadMappings, syncKeys } = useConsumableCrudConfig()
const syncSource = 'consumable-category-view-mappings'

const config = computed(() => ({
    title: '耗材分类',
    subtitle: '维护试剂、过滤耗材、玻璃器皿与生物耗材等分类。',
    endpoint: '/consumable-categories',
    codeProp: 'categoryCode',
    nameProp: 'categoryName',
    descriptionProp: 'description',
    syncKeys,
    relation: {
        endpoint: '/consumables',
        foreignKey: 'categoryId',
        title: '对应耗材',
        codeProp: 'consumableCode',
        nameProp: 'consumableName',
        metaProps: ['specification', 'storageLocation'],
        pageSize: 100,
        emptyText: '当前分类下暂无耗材。',
        editor: consumableConfig.value,
    },
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
}))

useCrudSync(syncKeys, loadMappings, syncSource)
onMounted(loadMappings)
</script>
