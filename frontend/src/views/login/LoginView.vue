<template>
    <div class="login-shell">
        <div class="login-aurora login-aurora-left"></div>
        <div class="login-aurora login-aurora-right"></div>

        <div class="login-card card-panel">
            <section class="login-story">
                <div class="eyebrow">{{ loginContent.platformEyebrow }}</div>
                <h1>{{ loginContent.title }}</h1>
                <p class="story-copy">{{ loginContent.description }}</p>

                <div class="story-grid">
                    <article v-for="item in loginContent.highlights" :key="item.title" class="story-panel">
                        <div class="story-panel-title">{{ item.title }}</div>
                        <div class="story-panel-value">{{ item.value }}</div>
                        <p>{{ item.description }}</p>
                    </article>
                </div>

                <ul class="story-list">
                    <li v-for="item in loginContent.valuePoints" :key="item">{{ item }}</li>
                </ul>

                <section class="story-usecases">
                    <div class="story-usecases-title">{{ loginContent.roleGuideTitle }}</div>
                    <p class="story-usecases-copy">{{ loginContent.roleGuideDescription }}</p>
                </section>
            </section>

            <section class="login-form-panel">
                <div class="form-head">
                    <div class="form-head-copy">
                        <div class="eyebrow">{{ mode === 'login' ? loginContent.loginModeEyebrow : loginContent.registerModeEyebrow }}</div>
                        <h2>{{ mode === 'login' ? loginContent.loginHeading : loginContent.registerHeading }}</h2>
                        <p>
                            {{ mode === 'login' ? loginContent.loginDescription : loginContent.registerDescription }}
                        </p>
                    </div>
                    <div class="form-badge">{{ loginContent.formBadge }}</div>
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

                    <el-form-item label="登录角色" prop="roleCode">
                        <div class="role-selector-panel">
                            <div v-if="roleLoading" class="role-card-empty">正在加载可选角色…</div>

                            <div v-else-if="!roleOptions.length" class="role-card-empty">
                                当前暂无可登录角色，请联系管理员检查角色配置。
                            </div>

                            <template v-else>
                                <div class="role-switch" role="tablist" aria-label="登录角色">
                                    <button
                                        v-for="item in roleOptions"
                                        :key="item.roleCode"
                                        type="button"
                                        class="role-switch-button"
                                        :class="{ active: loginForm.roleCode === item.roleCode }"
                                        :aria-pressed="loginForm.roleCode === item.roleCode"
                                        @click="selectRole(item.roleCode)"
                                    >
                                        {{ roleLabel(item) }}
                                    </button>
                                </div>

                                <div v-if="selectedRole" class="role-selected-summary">
                                    <div class="role-selected-title">
                                        <span>{{ roleLabel(selectedRole) }}</span>
                                        <span class="role-selected-code">{{ selectedRole.roleCode }}</span>
                                    </div>
                                    <p>{{ roleDescription(selectedRole) }}</p>
                                </div>
                            </template>
                        </div>
                    </el-form-item>

                    <el-form-item label="密码" prop="password">
                        <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
                    </el-form-item>

                    <div class="login-actions">
                        <el-button type="primary" size="large" :loading="loginLoading" @click="handleLogin">登录系统</el-button>
                        <div class="login-tip">登录后会自动切换到当前角色可见的菜单。</div>
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
                        <el-button type="primary" size="large" :loading="registerLoading" @click="handleRegister">创建账号</el-button>
                        <div class="login-tip">注册成功后会自动回到登录页，并预填新账号信息。</div>
                    </div>
                </el-form>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getAvailableRoles, getPublicLaboratories, register, type PublicLaboratoryOption } from '../../api/modules/auth'
import { loginContent } from '../../content/loginContent'
import { roleLabels } from '../../constants/roles'
import { useAuthStore } from '../../stores/auth'
import type { AvailableRole } from '../../types/auth'

const router = useRouter()
const authStore = useAuthStore()

const mode = ref<'login' | 'register'>('login')
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loginLoading = ref(false)
const registerLoading = ref(false)
const roleLoading = ref(false)
const laboratoryOptions = ref<PublicLaboratoryOption[]>([])
const roleOptions = ref<AvailableRole[]>([])
const selectedRole = computed(() => roleOptions.value.find((item) => item.roleCode === loginForm.roleCode))

const loginForm = reactive({
    username: '',
    roleCode: '',
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
    roleCode: [{ required: true, message: '请选择登录角色', trigger: 'change' }],
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
    email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
}

function roleLabel(role: AvailableRole) {
    return roleLabels[role.roleCode] || role.roleName || role.roleCode
}

function roleDescription(role: AvailableRole) {
    const description = role.description?.trim()
    if (!description || /^[?\uff1f\s]+$/.test(description)) {
        return defaultRoleDescription(role.roleCode)
    }
    return description
}

function defaultRoleDescription(roleCode: string) {
    const descriptions: Record<string, string> = {
        sys_admin: '管理用户、角色、菜单和系统配置。',
        lab_director: '统筹实验室资源、审批流程和安全管理。',
        equipment_admin: '处理设备台账、借还、维修与校准流程。',
        consumable_admin: '管理耗材台账、入库、出库和库存预警。',
        hazardous_admin: '跟踪危化品存放、领用、归还与处置。',
        teacher: '以教师身份发起借用、申请和业务查询。',
        student: '以学生身份在授权范围内进行借用与申请。',
        repair_staff: '专注设备报修、维修处理和结果回写。',
        calibration_staff: '专注校准任务、证书登记和到期维护。',
    }
    return descriptions[roleCode] || '选择该身份后进入对应的业务与权限视图。'
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

async function loadAvailableRoles() {
    roleLoading.value = true
    try {
        const result = await getAvailableRoles('')
        roleOptions.value = result.data.filter((item) => item.status === 1)
        if (!loginForm.roleCode && roleOptions.value.length === 1) {
            loginForm.roleCode = roleOptions.value[0].roleCode
        }
    } catch {
        roleOptions.value = []
    } finally {
        roleLoading.value = false
    }
}

function selectRole(roleCode: string) {
    loginForm.roleCode = roleCode
    loginFormRef.value?.validateField('roleCode').catch(() => false)
}

async function handleLogin() {
    const valid = await loginFormRef.value?.validate().catch(() => false)
    if (!valid) {
        return
    }
    loginLoading.value = true
    try {
        await authStore.login(loginForm.username, loginForm.password, loginForm.roleCode)
        ElMessage.success('登录成功')
        router.push('/dashboard')
    } catch (error) {
        const message = error instanceof Error ? error.message : '登录失败，请检查账号、角色和密码'
        ElMessage.error(message)
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
        loginForm.roleCode = result.data.roleCode
        roleOptions.value = [
            {
                id: 0,
                roleCode: result.data.roleCode,
                roleName: roleLabels[result.data.roleCode] || result.data.roleCode,
                description: '',
                status: 1,
            },
        ]
        mode.value = 'login'
        registerFormRef.value?.resetFields()
        ElMessage.success('注册成功，请登录')
    } catch (error) {
        const message = error instanceof Error ? error.message : '注册失败，请稍后重试'
        ElMessage.error(message)
    } finally {
        registerLoading.value = false
    }
}

onMounted(() => {
    void loadLaboratories()
    void loadAvailableRoles()
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
    width: 520px;
    height: 520px;
    border-radius: 999px;
    filter: blur(18px);
    opacity: 0.72;
    pointer-events: none;
}

.login-aurora-left {
    top: -100px;
    left: -140px;
    background: radial-gradient(circle, rgba(86, 200, 182, 0.34), transparent 68%);
}

.login-aurora-right {
    right: -100px;
    bottom: -160px;
    background: radial-gradient(circle, rgba(240, 190, 103, 0.3), transparent 66%);
}

.login-card {
    position: relative;
    width: min(1180px, 100%);
    display: grid;
    grid-template-columns: minmax(0, 1.2fr) minmax(420px, 0.9fr);
    gap: 26px;
    padding: 26px;
    background: linear-gradient(180deg, rgba(11, 18, 28, 0.92), rgba(8, 14, 22, 0.98));
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
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--line-soft);
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

.story-usecases {
    margin-top: 28px;
}

.story-usecases-title {
    font-size: 12px;
    letter-spacing: 0.16em;
    text-transform: uppercase;
    color: var(--text-soft);
}

.story-usecases-copy {
    margin: 12px 0 0;
    max-width: 560px;
    color: var(--text-secondary);
    line-height: 1.7;
}

.login-form-panel {
    padding: 18px;
    border-radius: 26px;
    background: linear-gradient(180deg, rgba(15, 24, 37, 0.96), rgba(10, 17, 27, 0.98));
    border: 1px solid var(--line-soft);
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
    background: rgba(86, 200, 182, 0.12);
    color: var(--accent);
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
    background: rgba(255, 255, 255, 0.04);
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
    background: rgba(255, 255, 255, 0.96);
    color: #08111a;
    box-shadow: 0 8px 18px rgba(18, 34, 40, 0.16);
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

.role-selector-panel {
    display: grid;
    gap: 12px;
}

.role-card-empty {
    display: grid;
    place-items: center;
    min-height: 92px;
    padding: 18px 16px;
    border-radius: 18px;
    border: 1px dashed rgba(255, 255, 255, 0.14);
    background: rgba(255, 255, 255, 0.03);
    color: var(--text-secondary);
    text-align: center;
    line-height: 1.7;
    font-size: 13px;
}

.role-switch {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 6px;
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.06);
}

.role-switch-button {
    border: 0;
    min-height: 42px;
    padding: 10px 16px;
    border-radius: 999px;
    background: transparent;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.role-switch-button.active {
    background: rgba(255, 255, 255, 0.96);
    color: #08111a;
    box-shadow: 0 8px 18px rgba(18, 34, 40, 0.16);
}

.role-selected-summary {
    padding: 14px 16px;
    border-radius: 18px;
    background: linear-gradient(135deg, rgba(86, 200, 182, 0.1), rgba(240, 190, 103, 0.12));
    border: 1px solid rgba(255, 255, 255, 0.08);
}

.role-selected-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    font-size: 15px;
    font-weight: 700;
    color: var(--text-main);
}

.role-selected-code {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 6px 10px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.08);
    color: var(--accent);
    font-size: 12px;
    font-weight: 700;
}

.role-selected-summary p {
    margin: 10px 0 0;
    color: var(--text-secondary);
    line-height: 1.7;
    font-size: 13px;
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

    .role-selected-title {
        align-items: flex-start;
        flex-direction: column;
    }
}
</style>
