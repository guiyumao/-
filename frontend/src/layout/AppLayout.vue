<template>
    <el-container class="page-shell" style="min-height: 100vh">
        <el-aside
            width="300px"
            style="background: var(--bg-sidebar); color: #f4efe7; padding: 24px 18px; border-right: 1px solid rgba(255,255,255,0.08)"
        >
            <div style="padding: 8px 12px 20px">
                <div style="font-size: 14px; letter-spacing: 0.18em; opacity: 0.7; text-transform: uppercase">Laboratory</div>
                <h1 style="margin: 10px 0 0; font-size: 28px; line-height: 1.1">实验室管理平台</h1>
                <p style="margin: 12px 0 0; line-height: 1.6; color: rgba(255,255,255,0.72)">
                    面向高校实验室设备、耗材、危化品与安全记录的一体化管理后台。
                </p>
            </div>

            <el-scrollbar height="calc(100vh - 180px)">
                <el-menu
                    :default-active="route.path"
                    background-color="transparent"
                    text-color="rgba(255,255,255,0.78)"
                    active-text-color="#ffffff"
                    unique-opened
                    style="border-right: none"
                    @select="handleSelect"
                >
                    <template v-for="item in menus" :key="item.id">
                        <el-sub-menu v-if="item.children?.length" :index="item.path || String(item.id)">
                            <template #title>
                                <span>{{ item.label }}</span>
                            </template>
                            <el-menu-item
                                v-for="child in item.children"
                                :key="child.path"
                                :index="child.path"
                                style="margin-bottom: 6px; border-radius: 12px; height: 44px"
                            >
                                {{ child.label }}
                            </el-menu-item>
                        </el-sub-menu>

                        <el-menu-item
                            v-else
                            :index="item.path"
                            style="margin-bottom: 8px; border-radius: 14px; height: 48px"
                        >
                            {{ item.label }}
                        </el-menu-item>
                    </template>
                </el-menu>
            </el-scrollbar>
        </el-aside>

        <el-container>
            <el-header style="height: 88px; padding: 24px 28px 0; background: transparent">
                <div class="card-panel" style="display: flex; justify-content: space-between; align-items: center; padding: 18px 22px">
                    <div>
                        <div style="font-size: 13px; color: var(--text-secondary); letter-spacing: 0.08em; text-transform: uppercase">
                            当前模块
                        </div>
                        <div style="font-size: 24px; font-weight: 700; margin-top: 6px">
                            {{ route.meta.title || '仪表盘' }}
                        </div>
                    </div>

                    <el-space>
                        <div style="text-align: right">
                            <div style="font-weight: 700">{{ authStore.user?.realName || '访客' }}</div>
                            <div style="font-size: 13px; color: var(--text-secondary)">{{ authStore.user?.username || '-' }}</div>
                        </div>
                        <el-button round @click="handleLogout">退出登录</el-button>
                    </el-space>
                </div>
            </el-header>

            <el-main style="padding: 20px 28px 28px">
                <RouterView />
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const menus = computed(() => authStore.sidebarMenus)

function handleSelect(index: string) {
    if (index.startsWith('/')) {
        router.push(index)
    }
}

function handleLogout() {
    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
}
</script>
