<template>
    <div class="login-shell">
        <div class="login-aurora login-aurora-left"></div>
        <div class="login-aurora login-aurora-right"></div>

        <div class="login-card card-panel">
            <section class="login-story">
                <div class="eyebrow">Laboratory Resource Hub</div>
                <h1>把实验室的日常流转整理成更清晰的操作界面</h1>
                <p class="story-copy">
                    从设备借还、耗材出入库到危化品使用登记与提醒报表，这里把高频工作收在一个更稳定、
                    更有秩序的后台里。
                </p>

                <div class="story-grid">
                    <article class="story-panel">
                        <div class="story-panel-title">管理主线</div>
                        <div class="story-panel-value">设备 / 耗材 / 安全</div>
                        <p>三条主线共用同一套权限、记录与提醒逻辑，避免数据分散。</p>
                    </article>
                    <article class="story-panel">
                        <div class="story-panel-title">面向角色</div>
                        <div class="story-panel-value">管理员 / 教师 / 实验员</div>
                        <p>适合实验室常见协作分工，重点信息在首页和导航里直接可见。</p>
                    </article>
                </div>

                <ul class="story-list">
                    <li>统一资产台账，减少重复录入和口径不一致。</li>
                    <li>重点提醒前置呈现，让校准、临期和库存风险更早暴露。</li>
                    <li>借还、维修、报表与审计串起来，方便复盘与追踪。</li>
                </ul>
            </section>

            <section class="login-form-panel">
                <div class="form-head">
                    <div class="form-head-copy">
                        <div class="eyebrow">{{ mode === 'login' ? 'Sign In' : 'Register' }}</div>
                        <h2>{{ mode === 'login' ? '进入系统' : '创建账号' }}</h2>
                        <p>
                            {{ mode === 'login'
                                ? '使用账号登录，继续处理今天的实验室事务。'
                                : '填写基础信息，创建一个教师或学生账号。' }}
                        </p>
                    </div>
                    <div class="form-badge">安全会话</div>
                </div>

                <div class="mode-switch">
                    <button class="mode-button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
                    <button class="mode-button" :class="{ active: mode === 'register' }" @click="switchToRegister">注册</button>
                </div>

                <el-form
                    v-if="mode === 'login'"
                    ref="loginFormRef"
                    :model="loginForm"
                    :rules="loginRules"
                    label-position="top"
                    class="login-form"
                >
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="loginForm.username" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
                    </el-form-item>

                    <div class="login-actions">
                        <el-button type="primary" size="large" :loading="loginLoading" @click="handleLogin">
                            登录系统
                        </el-button>
                        <div class="login-tip">请使用管理员分配的账号登录。首次部署请通过环境变量初始化管理员账号。</div>
                    </div>
                </el-form>

                <el-form
                    v-else
                    ref="registerFormRef"
                    :model="registerForm"
                    :rules="registerRules"
                    label-position="top"
                    class="login-form"
                >
                    <div class="register-grid">
                        <el-form-item label="真实姓名" prop="realName">
                            <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
                        </el-form-item>
                        <el-form-item label="账号类型" prop="registerType">
                            <el-select v-model="registerForm.registerType" placeholder="请选择账号类型">
                                <el-option label="教师" value="teacher" />
                                <el-option label="学生" value="student" />
                            </el-select>
                        </el-form-item>
                    </div>

                    <div class="register-grid">
                        <el-form-item label="用户名" prop="username">
                            <el-input v-model="registerForm.username" placeholder="请输入登录用户名" />
                        </el-form-item>
                        <el-form-item label="工号/学号" prop="userNo">
                            <el-input v-model="registerForm.userNo" placeholder="请输入工号或学号" />
                        </el-form-item>
                    </div>

                    <div class="register-grid">
                        <el-form-item label="密码" prop="password">
                            <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" />
                        </el-form-item>
                        <el-form-item label="确认密码" prop="confirmPassword">
                            <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
                        </el-form-item>
                    </div>

                    <el-form-item label="所属实验室" prop="laboratoryId">
                        <el-select v-model="registerForm.laboratoryId" placeholder="请选择实验室" filterable>
                            <el-option
                                v-for="item in laboratoryOptions"
                                :key="item.id"
                                :label="`${item.laboratoryName} (${item.laboratoryCode})`"
                                :value="item.id"
                            />
                        </el-select>
                    </el-form-item>

                    <div class="register-grid">
                        <el-form-item label="手机号" prop="phone">
                            <el-input v-model="registerForm.phone" placeholder="选填" />
                        </el-form-item>
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="registerForm.email" placeholder="选填" />
                        </el-form-item>
                    </div>

                    <div class="login-actions">
                        <el-button type="primary" size="large" :loading="registerLoading" @click="handleRegister">
                            创建账号
                        </el-button>
                        <div class="login-tip">注册成功后将返回登录页，并自动填入新账号。</div>
                    </div>
                </el-form>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getPublicLaboratories, register, type PublicLaboratoryOption } from '../../api/modules/auth'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const mode = ref<'login' | 'register'>('login')
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loginLoading = ref(false)
const registerLoading = ref(false)
const laboratoryOptions = ref<PublicLaboratoryOption[]>([])

const loginForm = reactive({
    username: '',
    password: '',
})

const registerForm = reactive({
    laboratoryId: undefined as number | undefined,
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    userNo: '',
    phone: '',
    email: '',
    registerType: 'teacher' as 'teacher' | 'student',
})

const loginRules: FormRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules: FormRules = {
    laboratoryId: [{ required: true, message: '请选择实验室', trigger: 'change' }],
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码至少 6 位', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
            validator: (_rule, value, callback) => {
                if (value !== registerForm.password) {
                    callback(new Error('两次输入的密码不一致'))
                    return
                }
                callback()
            },
            trigger: 'blur',
        },
    ],
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
    userNo: [{ required: true, message: '请输入工号或学号', trigger: 'blur' }],
    registerType: [{ required: true, message: '请选择账号类型', trigger: 'change' }],
    email: [
        {
            type: 'email',
            message: '请输入正确的邮箱格式',
            trigger: 'blur',
        },
    ],
}

function switchToRegister() {
    mode.value = 'register'
    if (laboratoryOptions.value.length === 0) {
        void loadLaboratories()
    }
}

async function loadLaboratories() {
    const result = await getPublicLaboratories()
    laboratoryOptions.value = result.data
}

async function handleLogin() {
    const valid = await loginFormRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    loginLoading.value = true
    try {
        await authStore.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        router.push('/dashboard')
    } finally {
        loginLoading.value = false
    }
}

async function handleRegister() {
    const valid = await registerFormRef.value?.validate().catch(() => false)
    if (!valid || !registerForm.laboratoryId) {
        return
    }

    registerLoading.value = true
    try {
        const result = await register({
            laboratoryId: registerForm.laboratoryId,
            username: registerForm.username,
            password: registerForm.password,
            realName: registerForm.realName,
            userNo: registerForm.userNo,
            phone: registerForm.phone || undefined,
            email: registerForm.email || undefined,
            registerType: registerForm.registerType,
        })

        loginForm.username = result.data.username
        loginForm.password = registerForm.password
        mode.value = 'login'
        registerFormRef.value?.resetFields()
        ElMessage.success('注册成功，请登录')
    } finally {
        registerLoading.value = false
    }
}

onMounted(() => {
    void loadLaboratories()
})
</script>

<style scoped>
.login-shell {
    position: relative;
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 28px;
    overflow: hidden;
}

.login-aurora {
    position: absolute;
    width: 460px;
    height: 460px;
    border-radius: 999px;
    filter: blur(16px);
    opacity: 0.7;
    pointer-events: none;
}

.login-aurora-left {
    top: -80px;
    left: -120px;
    background: radial-gradient(circle, rgba(18, 127, 114, 0.34), transparent 68%);
}

.login-aurora-right {
    right: -80px;
    bottom: -140px;
    background: radial-gradient(circle, rgba(212, 161, 76, 0.3), transparent 66%);
}

.login-card {
    position: relative;
    width: min(1180px, 100%);
    display: grid;
    grid-template-columns: minmax(0, 1.2fr) minmax(420px, 0.9fr);
    gap: 26px;
    padding: 26px;
}

.login-story {
    padding: 24px 16px 18px 10px;
}

.login-story h1 {
    margin: 14px 0 0;
    font-family: var(--font-display);
    font-size: clamp(34px, 5vw, 56px);
    line-height: 1.04;
    max-width: 12ch;
}

.story-copy {
    margin: 18px 0 0;
    max-width: 660px;
    color: var(--text-secondary);
    line-height: 1.85;
    font-size: 16px;
}

.story-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
    margin-top: 26px;
}

.story-panel {
    padding: 18px 18px 16px;
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.54);
    border: 1px solid rgba(21, 49, 59, 0.08);
}

.story-panel-title {
    font-size: 12px;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    color: var(--text-soft);
}

.story-panel-value {
    margin-top: 10px;
    font-family: var(--font-display);
    font-size: 22px;
    font-weight: 700;
}

.story-panel p {
    margin: 10px 0 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

.story-list {
    display: grid;
    gap: 12px;
    margin: 24px 0 0;
    padding: 0;
    list-style: none;
}

.story-list li {
    position: relative;
    padding-left: 18px;
    color: var(--text-main);
    line-height: 1.7;
}

.story-list li::before {
    content: "";
    position: absolute;
    left: 0;
    top: 11px;
    width: 8px;
    height: 8px;
    border-radius: 999px;
    background: linear-gradient(135deg, var(--accent), var(--accent-gold));
}

.login-form-panel {
    padding: 18px;
    border-radius: 26px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(251, 246, 238, 0.96));
    border: 1px solid rgba(21, 49, 59, 0.08);
}

.form-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 18px;
    margin-bottom: 20px;
}

.form-head-copy h2 {
    margin: 12px 0 0;
    font-family: var(--font-display);
    font-size: 30px;
}

.form-head-copy p {
    margin: 10px 0 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

.form-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 14px;
    border-radius: 999px;
    background: rgba(18, 127, 114, 0.1);
    color: var(--accent-deep);
    font-size: 13px;
    font-weight: 700;
}

.mode-switch {
    display: inline-grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    width: 100%;
    padding: 6px;
    margin-bottom: 22px;
    border-radius: 999px;
    background: rgba(18, 127, 114, 0.08);
}

.mode-button {
    border: 0;
    background: transparent;
    padding: 10px 14px;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 700;
    color: var(--text-secondary);
    cursor: pointer;
}

.mode-button.active {
    background: white;
    color: var(--accent-deep);
    box-shadow: 0 8px 18px rgba(18, 34, 40, 0.08);
}

.login-form :deep(.el-form-item__label) {
    font-weight: 700;
    color: var(--text-main);
}

.register-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
}

.login-actions {
    display: grid;
    gap: 14px;
    margin-top: 10px;
}

.login-actions .el-button {
    width: 100%;
    height: 52px;
    font-size: 15px;
}

.login-tip {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.7;
}

@media (max-width: 980px) {
    .login-card {
        grid-template-columns: 1fr;
    }

    .login-story {
        padding-right: 0;
    }
}

@media (max-width: 680px) {
    .login-shell {
        padding: 14px;
    }

    .login-card {
        padding: 16px;
    }

    .story-grid,
    .register-grid {
        grid-template-columns: 1fr;
    }

    .form-head {
        flex-direction: column;
    }
}
</style>
