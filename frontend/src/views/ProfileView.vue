<template>
  <div class="profile-page" :style="{ '--auth-bg': `url(${authBg})` }">
    <section class="profile-shell">
      <div class="profile-copy">
        <h1>个人中心</h1>
        <p>管理你的账号信息、订单入口和秒杀身份。登录后可以查看订单并参与秒杀活动。</p>
      </div>

      <el-card v-if="userStore.isLoggedIn" class="profile-card">
        <div class="profile-head">
          <div class="avatar">{{ avatarText }}</div>
          <div>
            <h2>{{ profile.username || userStore.username }}</h2>
            <span>{{ roleLabel }}</span>
          </div>
        </div>

        <div class="info-grid">
          <div>
            <label>邮箱</label>
            <strong>{{ profile.email || '未绑定' }}</strong>
          </div>
          <div>
            <label>用户角色</label>
            <strong>{{ roleLabel }}</strong>
          </div>
          <div>
            <label>注册时间</label>
            <strong>{{ formatDate(profile.createdAt) }}</strong>
          </div>
          <div>
            <label>账号状态</label>
            <strong>正常</strong>
          </div>
        </div>

        <div class="profile-actions">
          <el-button @click="openProfileDialog">编辑资料</el-button>
          <el-button @click="openPasswordDialog">修改密码</el-button>
          <el-button type="primary" @click="router.push('/orders')">我的订单</el-button>
          <el-button v-if="userStore.isAdmin" @click="router.push('/admin/dashboard')">管理后台</el-button>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-card>

      <el-card v-else class="auth-card">
        <el-tabs v-model="activeMode" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginRef" label-position="top" @submit.prevent="handleLogin">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
              </el-form-item>
              <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">登录</el-button>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerRef" label-position="top" @submit.prevent="handleRegister">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="registerForm.username" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="registerForm.email" placeholder="请输入邮箱（选填）" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
              </el-form-item>
              <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">注册</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </section>

    <el-dialog v-model="profileDialogVisible" title="编辑资料" width="420px" class="profile-dialog">
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-position="top">
        <el-form-item label="用户名">
          <el-input :model-value="profile.username || userStore.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px" class="profile-dialog">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="8-20位，需包含字母和数字" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPassword" @click="savePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'
import authBg from '@/assets/images/generated/auth-bg-v2.png'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loginRef = ref()
const registerRef = ref()
const profileFormRef = ref()
const passwordFormRef = ref()
const loading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const activeMode = ref(route.query.mode === 'register' ? 'register' : 'login')
const profile = ref({})

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '' })
const profileForm = reactive({ email: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, message: '至少3位', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,20}$/, message: '8-20位，需包含字母和数字', trigger: 'blur' }
  ]
}

const profileRules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,20}$/, message: '8-20位，需包含字母和数字', trigger: 'blur' }
  ]
}

const avatarText = computed(() => (profile.value.username || userStore.username || 'U').slice(0, 1).toUpperCase())
const roleLabel = computed(() => userStore.role === 'ADMIN' ? '管理员' : '普通用户')

watch(() => route.query.mode, mode => {
  activeMode.value = mode === 'register' ? 'register' : 'login'
})

async function loadProfile() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await authApi.me()
    profile.value = res.data || {}
  } catch {
    profile.value = { username: userStore.username, role: userStore.role }
  }
}

async function handleLogin() {
  try {
    await loginRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await authApi.login(loginForm)
    userStore.setUser(res.data.username, res.data.role)
    ElMessage.success('登录成功')
    await loadProfile()
    router.push(route.query.redirect || '/profile')
  } catch {
    // axios 拦截器已统一提示错误
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  try {
    await registerRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await authApi.register(registerForm)
    ElMessage.success('注册成功，请登录')
    activeMode.value = 'login'
    loginForm.username = registerForm.username
    registerForm.username = ''
    registerForm.email = ''
    registerForm.password = ''
  } catch {
    // axios 拦截器已统一提示错误
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  userStore.logout()
  profile.value = {}
  activeMode.value = 'login'
  router.push('/profile')
}

function openProfileDialog() {
  profileForm.email = profile.value.email || ''
  profileDialogVisible.value = true
}

function openPasswordDialog() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordDialogVisible.value = true
}

async function saveProfile() {
  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }
  savingProfile.value = true
  try {
    const res = await authApi.updateMe({ email: profileForm.email || null })
    profile.value = res.data || {}
    ElMessage.success('资料已更新')
    profileDialogVisible.value = false
  } catch {
    // axios 拦截器已统一提示错误
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }
  savingPassword.value = true
  try {
    await authApi.updatePassword(passwordForm)
    ElMessage.success('密码已修改，请使用新密码登录')
    passwordDialogVisible.value = false
    handleLogout()
  } catch {
    // axios 拦截器已统一提示错误
  } finally {
    savingPassword.value = false
  }
}

function formatDate(value) {
  if (!value) return '暂无记录'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 72px);
  padding: 64px max(24px, calc((100vw - 1200px) / 2));
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 255, 255, 0.86) 46%, rgba(255, 247, 247, 0.38) 100%),
    var(--auth-bg) center center / cover no-repeat;
}

.profile-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 460px);
  gap: 72px;
  align-items: center;
}

.profile-copy h1 {
  color: #172033;
  font-size: 44px;
  font-weight: 880;
  line-height: 1.08;
  margin-bottom: 18px;
}

.profile-copy p {
  max-width: 500px;
  color: #5e6678;
  font-size: 17px;
  line-height: 1.8;
}

.profile-card,
.auth-card {
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 28px 80px rgba(74, 25, 31, 0.13);
  backdrop-filter: blur(20px);
}

.profile-card :deep(.el-card__body),
.auth-card :deep(.el-card__body) {
  padding: 30px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 28px;
}

.avatar {
  width: 78px;
  height: 78px;
  display: grid;
  place-items: center;
  border-radius: 24px;
  color: #fff;
  font-size: 34px;
  font-weight: 850;
  background: linear-gradient(135deg, #ff6a5f 0%, #d71920 100%);
  box-shadow: 0 20px 42px rgba(215, 25, 32, 0.22);
}

.profile-head h2 {
  color: #172033;
  font-size: 26px;
  line-height: 1.2;
  margin-bottom: 6px;
}

.profile-head span {
  color: #e21d2b;
  font-size: 14px;
  font-weight: 720;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.info-grid div {
  padding: 16px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.78);
  border: 1px solid rgba(226, 232, 240, 0.78);
}

.info-grid label {
  display: block;
  color: #8a93a5;
  font-size: 12px;
  font-weight: 720;
  margin-bottom: 8px;
}

.info-grid strong {
  display: block;
  color: #172033;
  font-size: 15px;
  line-height: 1.35;
  word-break: break-all;
}

.profile-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  border-radius: 14px;
}

.auth-card :deep(.el-tabs__item) {
  height: 48px;
  font-size: 16px;
  font-weight: 760;
}

.auth-card :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 13px;
}

.profile-dialog :deep(.el-dialog) {
  border-radius: 20px;
}

.profile-dialog :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 13px;
}

@media (max-width: 760px) {
  .profile-page {
    min-height: calc(100vh - 120px);
    padding: 34px 18px 48px;
    background:
      linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.84) 54%, rgba(255, 247, 247, 0.72) 100%),
      var(--auth-bg) 66% bottom / auto 74% no-repeat;
  }
  .profile-shell { grid-template-columns: 1fr; gap: 28px; }
  .profile-copy h1 { font-size: 32px; }
  .profile-copy p { font-size: 15px; }
  .profile-card :deep(.el-card__body),
  .auth-card :deep(.el-card__body) { padding: 22px; }
  .info-grid { grid-template-columns: 1fr; }
}
</style>
