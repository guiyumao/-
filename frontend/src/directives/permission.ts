import type { App, DirectiveBinding } from 'vue'
import { useAuthStore } from '../stores/auth'

function updateVisibility(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const authStore = useAuthStore()
    const required = Array.isArray(binding.value) ? binding.value : [binding.value]
    const visible = required.filter(Boolean).every((permission) => authStore.hasPermission(permission))
    el.style.display = visible ? '' : 'none'
}

export function registerPermissionDirective(app: App) {
    app.directive('permission', {
        mounted(el, binding) {
            updateVisibility(el as HTMLElement, binding)
        },
        updated(el, binding) {
            updateVisibility(el as HTMLElement, binding)
        },
    })
}
