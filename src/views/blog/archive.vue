<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import ArticleCard from '@/components/ArticleCard.vue'
import { getArticles } from '@/api/article'
import { getCategoryDetail } from '@/api/category'
import { getTagDetail } from '@/api/tag'

const route = useRoute()
// 同一组件承载分类归档与标签归档，按路由名区分
const isCategory = computed(() => route.name === 'CategoryArchive')
const filterId = Number(route.params.id)

const loading = ref(false)
const articles = ref([])
const filterName = ref('')

const pagination = reactive({ page: 1, size: 10, total: 0 })

async function loadArticles() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, status: 1 }
    if (isCategory.value) params.categoryId = filterId
    else params.tagId = filterId
    const res = await getArticles(params)
    articles.value = res.data || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadFilterName() {
  try {
    if (isCategory.value) {
      const { data } = await getCategoryDetail(filterId)
      filterName.value = data?.name ?? `#${filterId}`
    } else {
      const { data } = await getTagDetail(filterId)
      filterName.value = data?.name ?? `#${filterId}`
    }
  } catch {
    filterName.value = `#${filterId}`
  }
}

function handlePageChange(page) {
  pagination.page = page
  loadArticles()
}

onMounted(() => {
  loadFilterName()
  loadArticles()
})
</script>

<template>
  <div v-loading="loading" class="archive-page">
    <h2 class="archive-title">
      {{ isCategory ? '分类' : '标签' }}：<span class="archive-name">{{ filterName }}</span>
      <span class="archive-count">共 {{ pagination.total }} 篇</span>
    </h2>

    <ArticleCard v-for="article in articles" :key="article.id" :article="article" class="archive-card" />
    <el-empty
      v-if="!loading && !articles.length"
      :description="`该${isCategory ? '分类' : '标签'}下暂无已发布文章`"
    />

    <el-pagination
      v-if="pagination.total > pagination.size"
      class="archive-pagination"
      background
      layout="total, prev, pager, next"
      :total="pagination.total"
      :current-page="pagination.page"
      :page-size="pagination.size"
      @current-change="handlePageChange"
    />
  </div>
</template>

<style scoped>
.archive-page {
  max-width: 820px;
  margin: 0 auto;
}

.archive-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.archive-name {
  color: #409eff;
}

.archive-count {
  margin-left: 12px;
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}

.archive-card {
  margin-bottom: 16px;
}

.archive-pagination {
  margin-top: 8px;
  justify-content: center;
}
</style>
