<script setup>
import { onMounted, reactive, ref } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getArticles } from '@/api/article'
import { getCategoryTree } from '@/api/category'
import { getAllTags } from '@/api/tag'
import { getActiveAnnouncements } from '@/api/announcement'

const loading = ref(false)
const articles = ref([])
const categories = ref([])
const tags = ref([])
const announcements = ref([])

const pagination = reactive({ page: 1, size: 10, total: 0 })

async function loadArticles() {
  loading.value = true
  try {
    const res = await getArticles({ page: pagination.page, size: pagination.size, status: 1 })
    articles.value = res.data || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.page = page
  loadArticles()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadArticles()
}

onMounted(async () => {
  loadArticles()
  // 侧边栏与公告加载失败不影响主列表
  getCategoryTree().then((res) => (categories.value = res.data || [])).catch(() => {})
  getAllTags().then((res) => (tags.value = res.data || [])).catch(() => {})
  getActiveAnnouncements(5)
    .then((res) => (announcements.value = res.data || []))
    .catch(() => {})
})
</script>

<template>
  <div v-loading="loading" class="blog-home">
    <div class="home-main">
      <!-- 公告区 -->
      <div v-if="announcements.length" class="announce-box">
        <el-alert
          v-for="item in announcements"
          :key="item.id"
          :title="`【公告】${item.title}`"
          :description="item.content"
          type="warning"
          :closable="false"
          show-icon
          class="announce-item"
        />
      </div>

      <!-- 文章列表 -->
      <ArticleCard v-for="article in articles" :key="article.id" :article="article" class="home-card" />
      <el-empty v-if="!loading && !articles.length" description="还没有已发布的文章" />

      <el-pagination
        v-if="pagination.total > pagination.size"
        class="home-pagination"
        background
        layout="total, prev, pager, next, sizes"
        :total="pagination.total"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 侧边栏 -->
    <aside class="home-aside">
      <el-card shadow="never" class="aside-card">
        <template #header>分类</template>
        <ul class="aside-list">
          <li v-for="cat in categories" :key="cat.id">
            <router-link :to="`/category/${cat.id}`" class="aside-link">
              {{ cat.name }}
              <span class="aside-count">{{ cat.articleCount ?? '' }}</span>
            </router-link>
          </li>
          <li v-if="!categories.length" class="aside-empty">暂无分类</li>
        </ul>
      </el-card>
      <el-card shadow="never" class="aside-card">
        <template #header>标签云</template>
        <div class="aside-tags">
          <router-link v-for="tag in tags" :key="tag.id" :to="`/tag/${tag.id}`" class="aside-tag-link">
            <el-tag size="small" effect="plain" class="aside-tag">{{ tag.name }}</el-tag>
          </router-link>
          <span v-if="!tags.length" class="aside-empty">暂无标签</span>
        </div>
      </el-card>
    </aside>
  </div>
</template>

<style scoped>
.blog-home {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.home-main {
  flex: 1;
  min-width: 0;
}

.announce-box {
  margin-bottom: 16px;
}

.announce-item {
  margin-bottom: 8px;
}

.announce-item:last-child {
  margin-bottom: 0;
}

.home-card {
  margin-bottom: 16px;
}

.home-pagination {
  margin-top: 8px;
  justify-content: center;
}

.home-aside {
  width: 260px;
  flex-shrink: 0;
}

.aside-card {
  border-radius: 6px;
  margin-bottom: 16px;
}

.aside-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.aside-list li {
  padding: 6px 0;
  border-bottom: 1px dashed #f0f2f5;
}

.aside-list li:last-child {
  border-bottom: none;
}

.aside-link {
  display: flex;
  justify-content: space-between;
  color: #303133;
  font-size: 14px;
  text-decoration: none;
}

.aside-link:hover {
  color: #409eff;
}

.aside-count {
  color: #c0c4cc;
  font-size: 12px;
}

.aside-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.aside-tag-link {
  text-decoration: none;
}

.aside-empty {
  color: #c0c4cc;
  font-size: 13px;
}

@media (max-width: 900px) {
  .home-aside {
    display: none;
  }
}
</style>
