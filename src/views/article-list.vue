<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  changeArticleStatus,
  changeArticleTop,
  deleteArticle,
  getArticles,
} from '@/api/article'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined,
})

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已下架' },
]

const statusTag = { 0: 'info', 1: 'success', 2: 'warning' }
const statusLabel = { 0: '草稿', 1: '已发布', 2: '已下架' }

async function load() {
  loading.value = true
  try {
    const res = await getArticles({
      page: query.page,
      size: query.size,
      keyword: query.keyword.trim() || undefined,
      status: query.status,
    })
    articles.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  load()
}

function handleReset() {
  query.keyword = ''
  query.status = undefined
  handleSearch()
}

function handlePageChange(page) {
  query.page = page
  load()
}

function handleSizeChange(size) {
  query.size = size
  query.page = 1
  load()
}

async function handlePublish(row) {
  await changeArticleStatus(row.id, 1)
  ElMessage.success('已发布')
  load()
}

async function handleOffline(row) {
  await changeArticleStatus(row.id, 2)
  ElMessage.success('已下架')
  load()
}

async function handleToggleTop(row) {
  const next = row.isTop === 1 ? 0 : 1
  await changeArticleTop(row.id, next)
  ElMessage.success(next === 1 ? '已置顶' : '已取消置顶')
  load()
}

async function handleDelete(row) {
  const ok = await ElMessageBox.confirm(
    `确定删除文章「${row.title}」吗？删除后不可恢复。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
  ).then(() => true).catch(() => false)
  if (!ok) return

  await deleteArticle(row.id)
  ElMessage.success('删除成功')
  // 当前页删空后回退一页
  if (articles.value.length === 1 && query.page > 1) query.page -= 1
  load()
}

onMounted(load)
</script>

<template>
  <div class="list-page">
    <div class="list-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题 / 摘要"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select
        v-model="query.status"
        placeholder="全部状态"
        clearable
        style="width: 140px"
        @change="handleSearch"
      >
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button :icon="Search" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="toolbar-spacer" />
      <el-button type="primary" :icon="Plus" @click="router.push('/admin/article/edit')">
        新增文章
      </el-button>
    </div>

    <el-table v-loading="loading" :data="articles" class="list-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="标题" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" :underline="false" @click="router.push(`/admin/article/edit/${row.id}`)">
            {{ row.title }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="作者" width="110">
        <template #default="{ row }">{{ row.authorName || row.authorUsername }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag[row.status]" size="small">{{ statusLabel[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="70" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="danger" size="small" effect="dark">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="浏览 / 赞 / 评" width="110" align="center">
        <template #default="{ row }">
          {{ row.viewCount ?? 0 }} / {{ row.likeCount ?? 0 }} / {{ row.commentCount ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status !== 1"
            link
            type="primary"
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button v-else link type="warning" size="small" @click="handleOffline(row)">
            下架
          </el-button>
          <el-button link size="small" @click="handleToggleTop(row)">
            {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button
            link
            size="small"
            @click="router.push(`/admin/article/edit/${row.id}`)"
          >
            编辑
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="list-pagination"
      background
      layout="total, prev, pager, next, sizes"
      :total="total"
      :current-page="query.page"
      :page-size="query.size"
      :page-sizes="[10, 20, 50]"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<style scoped>
.list-page {
  max-width: 1200px;
  margin: 0 auto;
}

.list-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar-spacer {
  flex: 1;
}

.list-table {
  border-radius: 6px;
}

.list-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
