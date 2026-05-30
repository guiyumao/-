<template>
    <div class="login-shell">
        <div class="login-card card-panel">
            <div class="login-copy">
                <div class="eyebrow">LAB MANAGEMENT SYSTEM</div>
                <h1>实验室设备与耗材管理平台</h1>
                <p>支持设备借用、耗材出入库、危化品领用、安全记录、到期提醒与报表分析的一体化实验室管理系统。</p>
            </div>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="form.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
                </el-form-item>
                <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">
                    登录系统
                </el-button>
                <p class="login-tip">演示账号默认使用 `admin` / `admin123`。</p>
            </el-form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
    username: 'admin',
    password: 'admin123',
})

const rules: FormRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    loading.value = true
    try {
        await authStore.login(form.username, form.password)
        ElMessage.success('登录成功')
        router.push('/dashboard')
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.login-shell {
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 24px;
}

.login-card {
    width: min(1040px, 100%);
    display: grid;
    grid-template-columns: 1.2fr 0.9fr;
    gap: 32px;
    padding: 32px;
}

.login-copy {
    padding: 20px 10px 20px 8px;
}

.eyebrow {
    font-size: 13px;
    letter-spacing: 0.2em;
    color: var(--accent);
}

.login-copy h1 {
    margin: 18px 0 0;
    font-size: 42px;
    line-height: 1.1;
}

.login-copy p {
    margin-top: 18px;
    color: var(--text-secondary);
    line-height: 1.9;
    max-width: 560px;
}

.login-form {
    padding: 16px;
}

.login-tip {
    margin-top: 16px;
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.7;
}
</style>
