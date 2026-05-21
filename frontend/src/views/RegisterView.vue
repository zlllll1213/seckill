<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <img :src="heroImage" alt="" class="auth-visual" />
      <div class="brand">秒杀系统</div>
      <h2>创建账号</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">注册</el-button>
        <div class="link-row">已有账号？<router-link to="/login">立即登录</router-link></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import heroImage from '@/assets/images/generated/auth-bg-v2.png'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = ref({ username: '', email: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, message: '至少3位', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,20}$/, message: '8-20位，需包含字母和数字', trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await authApi.register(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // axios 拦截器已统一提示错误
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 24px;
  background:
    linear-gradient(90deg, rgba(6, 7, 8, 0.98), rgba(6, 7, 8, 0.66)),
    url('@/assets/images/generated/auth-bg-v2.png') center / cover no-repeat;
}
.auth-card {
  width: min(420px, 100%);
  padding: 18px;
  border: 1px solid var(--line-strong);
  border-radius: 2px;
  background: rgba(10, 12, 14, 0.86);
  box-shadow: var(--shadow-panel);
  backdrop-filter: blur(20px);
}
.brand { color: #ff3a32; font-size: 14px; font-weight: 900; text-align: center; margin-bottom: 6px; }
.auth-visual { display: block; width: 100%; aspect-ratio: 16 / 9; object-fit: cover; border-radius: 2px; margin-bottom: 18px; filter: saturate(0.9) contrast(1.1); }
h2 { margin-bottom: 24px; text-align: center; color: var(--text); font-family: var(--font-display); font-size: 34px; }
.submit-btn { width: 100%; margin-top: 8px; }
.link-row { color: var(--muted); text-align: center; margin-top: 12px; font-size: 14px; }
.link-row a { color: #ff3a32; }
</style>
