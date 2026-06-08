<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import CrudPage from '../shared/CrudPage.vue'
import { useEquipmentCrudConfig } from './useEquipmentCrudConfig'
import { useCrudSync } from '../../utils/crudSync'

const { config, loadMappings, syncKeys } = useEquipmentCrudConfig()
const syncSource = 'equipment-view-mappings'

useCrudSync(syncKeys, loadMappings, syncSource)
onMounted(loadMappings)
</script>
