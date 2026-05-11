<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="login-logo">🐾</span>
        <h2>宠迹后台管理</h2>
        <p>Pet Trail Admin</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item v-if="requireTotp" prop="totpCode">
          <el-input v-model="form.totpCode" placeholder="请输入2FA验证码" prefix-icon="Key" size="large" maxlength="6" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">{{ requireTotp ? '验证并登录' : '登 录' }}</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/admin'
import { useAdminStore } from '@/store/admin'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)
const requireTotp = ref(false)

const form = reactive({ username: '', password: '', totpCode: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (requireTotp.value && !form.totpCode) {
    ElMessage.warning('请输入2FA验证码'); return
  }
  loading.value = true
  try {
    const data = { username: form.username, password: form.password }
    if (requireTotp.value) {
      data.totpCode = Number(form.totpCode)
    }
    const res = await login(data)
    if (res.success && res.data) {
      if (res.data.requireTotp) {
        requireTotp.value = true
        ElMessage.info('该账号已开启2FA，请输入验证码')
        loading.value = false
        return
      }
      adminStore.setLoginInfo(res.data.token, res.data.admin, res.data.menus)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch (e) {
    if (e.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6a3d 0%, #ff3d6a 100%);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-logo {
  font-size: 48px;
}
.login-header h2 {
  margin: 8px 0 4px;
  color: #333;
}
.login-header p {
  color: #999;
  font-size: 14px;
}
</style>
