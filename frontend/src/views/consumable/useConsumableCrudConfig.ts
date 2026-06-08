import { computed, ref } from 'vue'
import {
    fetchConsumableCategoryOptions,
    fetchLaboratoryOptions,
    fetchUserOptions,
    type SelectOption,
} from '../../api/modules/business'

function toMap(options: SelectOption[]) {
    return options.reduce<Record<string, string>>((acc, item) => {
        acc[String(item.id)] = item.label
        return acc
    }, {})
}

export function useConsumableCrudConfig() {
    const laboratoryMap = ref<Record<string, string>>({})
    const categoryMap = ref<Record<string, string>>({})
    const userMap = ref<Record<string, string>>({})
    const laboratoryOptions = ref<SelectOption[]>([])
    const categoryOptions = ref<SelectOption[]>([])
    const userOptions = ref<SelectOption[]>([])

    async function loadMappings() {
        const [laboratories, categories, users] = await Promise.all([
            fetchLaboratoryOptions(),
            fetchConsumableCategoryOptions(),
            fetchUserOptions(),
        ])

        laboratoryOptions.value = laboratories
        categoryOptions.value = categories
        userOptions.value = users
        laboratoryMap.value = toMap(laboratories)
        categoryMap.value = toMap(categories)
        userMap.value = toMap(users)
    }

    const syncKeys = ['/consumables', '/consumable-categories']

    const config = computed(() => ({
        title: '耗材台账',
        subtitle: '维护耗材编码、规格、库存阈值与归属信息。',
        endpoint: '/consumables',
        createPermission: 'consumable:edit',
        editPermission: 'consumable:edit',
        deletePermission: 'consumable:edit',
        createText: '新增耗材',
        syncKeys,
        displayMappings: {
            laboratoryId: laboratoryMap.value,
            categoryId: categoryMap.value,
            managerUserId: userMap.value,
        },
        columns: [
            { label: '实验室', prop: 'laboratoryId', type: 'select' as const, options: laboratoryOptions.value },
            { label: '分类', prop: 'categoryId', type: 'select' as const, options: categoryOptions.value },
            { label: '负责人', prop: 'managerUserId', type: 'select' as const, options: userOptions.value },
            { label: '耗材编码', prop: 'consumableCode' },
            { label: '耗材名称', prop: 'consumableName' },
            { label: '规格型号', prop: 'specification' },
            { label: '单位', prop: 'unit' },
            { label: '存放位置', prop: 'storageLocation' },
        ],
        emptyRecord: () => ({
            laboratoryId: 1,
            categoryId: 1,
            managerUserId: 4,
            consumableCode: '',
            consumableName: '',
            specification: '',
            unit: '',
            unitPrice: 0,
            safetyStock: 0,
            maxStock: 0,
            storageLocation: '',
            expiryRequired: 1,
            status: 1,
        }),
    }))

    return {
        config,
        loadMappings,
        syncKeys,
    }
}
