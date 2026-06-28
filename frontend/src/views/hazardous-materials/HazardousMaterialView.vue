<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../shared/CrudPage.vue'
import {
    fetchLaboratoryOptions,
    firstOptionId,
    fetchUserOptions,
    optionsToMap,
    type SelectOption,
} from '../../api/modules/business'

const laboratoryMap = ref<Record<string, string>>({})
const userMap = ref<Record<string, string>>({})
const laboratoryOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])

async function loadMappings() {
    const [laboratories, users] = await Promise.all([
        fetchLaboratoryOptions(),
        fetchUserOptions(),
    ])
    laboratoryOptions.value = laboratories
    userOptions.value = users
    laboratoryMap.value = optionsToMap(laboratories)
    userMap.value = optionsToMap(users)
}

const config = computed(() => ({
    title: '危化品台账',
    subtitle: '维护危化品编码、CAS 号、危险类别与存储位置。',
    endpoint: '/hazardous-materials',
    createPermission: 'hazardous_material:edit',
    editPermission: 'hazardous_material:edit',
    deletePermission: 'hazardous_material:edit',
    createText: '新增危化品',
    displayMappings: {
        laboratoryId: laboratoryMap.value,
        managerUserId: userMap.value,
    },
    columns: [
        { label: '实验室', prop: 'laboratoryId', type: 'select' as const, options: laboratoryOptions.value },
        { label: '负责人', prop: 'managerUserId', type: 'select' as const, options: userOptions.value },
        { label: '危化品编码', prop: 'hazardousCode' },
        { label: '物料名称', prop: 'materialName' },
        { label: 'CAS 号', prop: 'casNo' },
        { label: '危险类别', prop: 'hazardCategory' },
        { label: '规格型号', prop: 'specification' },
        { label: '存放位置', prop: 'storageLocation' },
    ],
    emptyRecord: () => ({
        laboratoryId: firstOptionId(laboratoryOptions.value) ?? 0,
        managerUserId: firstOptionId(userOptions.value) ?? 0,
        hazardousCode: '',
        materialName: '',
        casNo: '',
        hazardCategory: '',
        specification: '',
        unit: '',
        concentration: '',
        storageLocation: '',
        safetyStock: 0,
        msdsCode: '',
        status: 1,
    }),
}))

onMounted(loadMappings)
</script>
