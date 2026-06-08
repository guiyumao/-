<template>
    <CategoryCardPage :config="config" />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import CategoryCardPage from '../shared/CategoryCardPage.vue'
import { useEquipmentCrudConfig } from '../equipment/useEquipmentCrudConfig'
import { useCrudSync } from '../../utils/crudSync'

const { config: equipmentConfig, loadMappings, syncKeys } = useEquipmentCrudConfig()
const syncSource = 'equipment-category-view-mappings'

const config = computed(() => ({
    title: '设备分类',
    subtitle: '维护分析仪器、精密仪器与通用设备等分类。',
    endpoint: '/equipment-categories',
    codeProp: 'categoryCode',
    nameProp: 'categoryName',
    descriptionProp: 'description',
    syncKeys,
    relation: {
        endpoint: '/equipment',
        foreignKey: 'categoryId',
        title: '对应设备',
        codeProp: 'equipmentCode',
        nameProp: 'equipmentName',
        metaProps: ['model', 'storageLocation'],
        pageSize: 100,
        emptyText: '当前分类下暂无设备。',
        editor: equipmentConfig.value,
    },
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
}))

useCrudSync(syncKeys, loadMappings, syncSource)
onMounted(loadMappings)
</script>
