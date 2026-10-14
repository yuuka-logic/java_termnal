<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function handleCommand(command) {
  if (command === 'logout') {
    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (command === 'admin') {
    router.push('/admin/articles')
  }
}
</script>

<template>
  <div class="blog-layout">
    <header class="blog-header">
      <div class="blog-header-inner">
        <router-link to="/" class="blog-logo">个人博客</router-link>
        <nav class="blog-nav">
          <router-link to="/" class="blog-nav-link">首页</router-link>
        </nav>
        <div class="blog-header-right">
          <template v-if="authStore.token">
            <el-button type="primary" size="small" @click="router.push('/admin/article/edit')">
              写文章
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="blog-user">
                {{ authStore.user?.nickname || authStore.user?.username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="admin">文章管理</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="blog-nav-link">登录</router-link>
            <el-button type="primary" size="small" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="blog-main">
      <router-view />
    </main>

    <footer class="blog-footer">个人博客 · Vue3 + Spring Boot</footer>
  </div>
</template>

<style scoped>
.blog-layout {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.blog-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 10;
}

.blog-header-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  max-width: 1080px;
  height: 56px;
  margin: 0 auto;
  padding: 0 20px;
}

.blog-logo {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  text-decoration: none;
}

.blog-nav {
  display: flex;
  gap: 16px;
  flex: 1;
}

.blog-nav-link {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
}

.blog-nav-link:hover {
  color: #409eff;
}

.blog-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.blog-user {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  outline: none;
}

.blog-main {
  flex: 1;
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 20px;
}

.blog-footer {
  padding: 16px 0;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
