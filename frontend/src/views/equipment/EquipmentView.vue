<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../shared/CrudPage.vue'
import {
    fetchEquipmentCategoryOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    type SelectOption,
} from '../../api/modules/business'

const laboratoryMap = ref<Record<string, string>>({})
const categoryMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const laboratoryOptions = ref<SelectOption[]>([])
const categoryOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])

function toMap(options: SelectOption[]) {
    return options.reduce<Record<string, string>>((acc, item) => {
        acc[String(item.id)] = item.label
        return acc
    }, {})
}

async function loadMappings() {
    const [laboratories, categories, users] = await Promise.all([
        fetchLaboratoryOptions(),
        fetchEquipmentCategoryOptions(),
        fetchUserOptions(),
    ])
    laboratoryOptions.value = laboratories
    categoryOptions.value = categories
    userOptions.value = users
    laboratoryMap.value = toMap(laboratories)
    categoryMap.value = toMap(categories)
    userMap.value = toMap(users)
}

const config = computed(() => ({
    title: '设备台账',
    subtitle: '管理设备编码、型号、状态、采购信息与校准周期。',
    endpoint: '/equipment',
    createPermission: 'equipment:edit',
    editPermission: 'equipment:edit',
    deletePermission: 'equipment:edit',
    createText: '新增设备',
    displayMappings: {
        laboratoryId: laboratoryMap.value,
        categoryId: categoryMap.value,
        managerUserId: userMap.value,
    },
    columns: [
        { label: '实验室', prop: 'laboratoryId', type: 'select' as const, options: laboratoryOptions.value },
        { label: '分类', prop: 'categoryId', type: 'select' as const, options: categoryOptions.value },
        { label: '负责人', prop: 'managerUserId', type: 'select' as const, options: userOptions.value },
        { label: '设备编码', prop: 'equipmentCode' },
        { label: '设备名称', prop: 'equipmentName' },
        { label: '型号', prop: 'model' },
        { label: '品牌', prop: 'brand' },
        { label: '存放位置', prop: 'storageLocation' },
    ],
    emptyRecord: () => ({
        laboratoryId: 1,
        categoryId: 1,
        managerUserId: 3,
        equipmentCode: '',
        equipmentName: '',
        model: '',
        brand: '',
        manufacturer: '',
        purchaseDate: '',
        purchasePrice: 0,
        serviceLifeYears: 5,
        storageLocation: '',
        status: 1,
        calibrationCycleDays: 365,
    }),
}))

onMounted(loadMappings)
</script>
