<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <img :src="heroImage" alt="" class="auth-visual" />
      <div class="brand">秒杀系统</div>
      <h2>欢迎回来</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">登录</el-button>
        <div class="link-row">没有账号？<router-link to="/register">立即注册</router-link></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'
import heroImage from '@/assets/images/generated/auth-bg-v2.png'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await authApi.login(form.value)
    userStore.setUser(res.data.username, res.data.role)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/products')
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
    linear-gradient(90deg, rgba(255, 255, 255, 0.94), rgba(255, 247, 247, 0.5)),
    url('@/assets/images/generated/auth-bg-v2.png') center / cover no-repeat;
}
.auth-card {
  width: min(420px, 100%);
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 28px 80px rgba(74, 25, 31, 0.13);
  backdrop-filter: blur(20px);
}
.brand { color: #e64545; font-size: 14px; font-weight: 800; text-align: center; margin-bottom: 6px; }
.auth-visual { display: block; width: 100%; aspect-ratio: 16 / 9; object-fit: cover; border-radius: 8px; margin-bottom: 18px; }
h2 { margin-bottom: 24px; text-align: center; color: #1f2937; }
.submit-btn { width: 100%; margin-top: 8px; }
.link-row { text-align: center; margin-top: 12px; font-size: 14px; }
</style>
