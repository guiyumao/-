export interface PageResult<T> {
    current: number
    pageSize: number
    total: number
    pages: number
    records: T[]
}

export interface ApiResult<T> {
    code: number
    message: string
    data: T
}

export interface CrudColumn {
    label: string
    prop: string
    width?: number
    type?: 'text' | 'textarea' | 'number' | 'datetime' | 'select'
    options?: CrudOption[]
    placeholder?: string
}

export interface CrudOption {
    label: string
    value: string | number
}

export interface CrudModuleConfig<T extends Record<string, unknown>> {
    title: string
    subtitle: string
    endpoint: string
    columns: CrudColumn[]
    emptyRecord: () => T
    createPermission?: string
    editPermission?: string
    deletePermission?: string
    createText?: string
    displayMappings?: Record<string, Record<string, string>>
}
