import { onBeforeUnmount, onMounted } from 'vue'

const CRUD_SYNC_EVENT = 'lab-crud-sync'
const CRUD_SYNC_STORAGE_KEY = 'lab-crud-sync'
let crudSyncChannel: BroadcastChannel | null = null

interface CrudSyncDetail {
    keys: string[]
    source?: string
    timestamp: number
}

function normalizeKeys(keys: string[]) {
    return [...new Set(keys.filter((key) => key.trim().length > 0))]
}

function getCrudSyncChannel() {
    if (typeof window === 'undefined' || typeof BroadcastChannel === 'undefined') {
        return null
    }

    crudSyncChannel ??= new BroadcastChannel(CRUD_SYNC_EVENT)
    return crudSyncChannel
}

function dispatchCrudSync(detail: CrudSyncDetail) {
    window.dispatchEvent(
        new CustomEvent<CrudSyncDetail>(CRUD_SYNC_EVENT, {
            detail,
        }),
    )
}

export function emitCrudSync(keys: string[], source?: string) {
    if (typeof window === 'undefined') {
        return
    }

    const normalizedKeys = normalizeKeys(keys)
    if (!normalizedKeys.length) {
        return
    }

    const detail = {
        keys: normalizedKeys,
        source,
        timestamp: Date.now(),
    }

    dispatchCrudSync(detail)
    getCrudSyncChannel()?.postMessage(detail)

    try {
        window.localStorage.setItem(CRUD_SYNC_STORAGE_KEY, JSON.stringify(detail))
    } catch {
        // Ignore storage failures and keep the in-tab sync path available.
    }
}

export function useCrudSync(keys: string[], handler: () => void | Promise<void>, source?: string) {
    if (typeof window === 'undefined') {
        return
    }

    const normalizedKeys = normalizeKeys(keys)
    if (!normalizedKeys.length) {
        return
    }

    const runHandler = (detail?: CrudSyncDetail | null) => {
        if (!detail || detail.source === source) {
            return
        }

        if (detail.keys.some((key) => normalizedKeys.includes(key))) {
            void handler()
        }
    }

    const eventListener = (event: Event) => {
        runHandler((event as CustomEvent<CrudSyncDetail>).detail)
    }

    const channelListener = (event: MessageEvent<CrudSyncDetail>) => {
        runHandler(event.data)
    }

    const storageListener = (event: StorageEvent) => {
        if (event.key !== CRUD_SYNC_STORAGE_KEY || !event.newValue) {
            return
        }

        try {
            runHandler(JSON.parse(event.newValue) as CrudSyncDetail)
        } catch {
            // Ignore malformed payloads from older sessions.
        }
    }

    onMounted(() => {
        window.addEventListener(CRUD_SYNC_EVENT, eventListener as EventListener)
        window.addEventListener('storage', storageListener)
        getCrudSyncChannel()?.addEventListener('message', channelListener as EventListener)
    })

    onBeforeUnmount(() => {
        window.removeEventListener(CRUD_SYNC_EVENT, eventListener as EventListener)
        window.removeEventListener('storage', storageListener)
        getCrudSyncChannel()?.removeEventListener('message', channelListener as EventListener)
    })
}
