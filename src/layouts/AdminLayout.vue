<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <div class="admin-layout">
    <header class="admin-header">
      <div class="admin-header-inner">
        <span class="admin-title">个人博客 · 管理后台</span>
        <div class="admin-header-right">
          <el-button text :icon="Back" @click="router.push('/')">返回前台</el-button>
          <span class="admin-user">{{ authStore.user?.nickname || authStore.user?.username }}</span>
          <el-button text type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </header>

    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100%;
}

.admin-header {
  background: #1f2d3d;
}

.admin-header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 24px;
}

.admin-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.admin-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-header-right :deep(.el-button) {
  color: #cfd8e3;
}

.admin-user {
  font-size: 14px;
  color: #fff;
  padding: 0 8px;
}

.admin-main {
  padding: 20px 24px 40px;
}
</style>
