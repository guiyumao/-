<template>
    <CrudPage :config="config" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import CrudPage from '../shared/CrudPage.vue'
import { useConsumableCrudConfig } from './useConsumableCrudConfig'
import { useCrudSync } from '../../utils/crudSync'

const { config, loadMappings, syncKeys } = useConsumableCrudConfig()
const syncSource = 'consumable-view-mappings'

useCrudSync(syncKeys, loadMappings, syncSource)
onMounted(loadMappings)
</script>
