import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

/**
 * 路由分区：
 * - 前台（BlogLayout）：公开浏览，无需登录
 * - /login /register：独立页面
 * - 后台（AdminLayout）：写操作需 token，meta.requiresAuth 统一守卫
 */
const routes = [
  {
    path: '/',
    component: () => import('@/layouts/BlogLayout.vue'),
    children: [
      {
        path: '',
        name: 'BlogHome',
        component: () => import('@/views/blog/blog-index.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/blog/post-detail.vue'),
        meta: { title: '文章详情' },
      },
      {
        path: 'category/:id',
        name: 'CategoryArchive',
        component: () => import('@/views/blog/archive.vue'),
        meta: { title: '分类归档' },
      },
      {
        path: 'tag/:id',
        name: 'TagArchive',
        component: () => import('@/views/blog/archive.vue'),
        meta: { title: '标签归档' },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/articles' },
      {
        path: 'articles',
        name: 'ArticleList',
        component: () => import('@/views/article-list.vue'),
        meta: { requiresAuth: true, title: '文章管理' },
      },
      {
        // :id? 可选参数：不带 id 为新增，带 id 为编辑
        path: 'article/edit/:id?',
        name: 'ArticleEdit',
        component: () => import('@/views/article-edit.vue'),
        meta: { requiresAuth: true, title: '文章编辑' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('pb_token')) {
    ElMessage.warning('请先登录')
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 个人博客` : '个人博客'
})

export default router
