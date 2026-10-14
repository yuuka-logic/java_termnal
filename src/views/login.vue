<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    // 支持 ?redirect=... 登录后回跳到原页面
    router.replace(route.query.redirect || '/article/edit')
  } catch {
    // 失败提示已由 request.js 统一弹出
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <h2 class="login-title">个人博客管理后台</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="login-tip">默认测试账号：admin / 123456</p>
      <p class="login-tip">
        没有账号？
        <router-link :to="{ path: '/register', query: route.query }" class="login-link">立即注册</router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b4a6f 100%);
}

.login-card {
  width: 380px;
  padding: 8px 12px 0;
}

.login-title {
  margin: 8px 0 24px;
  font-size: 20px;
  font-weight: 600;
  text-align: center;
  color: #303133;
}

.login-btn {
  width: 100%;
}

.login-tip {
  margin: 0 0 8px;
  font-size: 12px;
  text-align: center;
  color: #909399;
}

.login-tip:last-child {
  margin-bottom: 16px;
}

.login-link {
  color: #409eff;
  text-decoration: none;
}
</style>
