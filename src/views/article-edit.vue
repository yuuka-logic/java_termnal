<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Check } from '@element-plus/icons-vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { createArticle, getArticleDetail, updateArticle } from '@/api/article'
import { getCategoryTree } from '@/api/category'
import { getAllTags } from '@/api/tag'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// /article/edit/:id? —— 有 id 为编辑，无 id 为新增
const idParam = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
const articleId = idParam && /^\d+$/.test(idParam) ? Number(idParam) : null
const isEdit = computed(() => articleId !== null)

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()

const categoryTree = ref([])
const tagOptions = ref([])
// 编辑模式下保存详情，用于头部元信息展示
const detail = ref(null)

function createDefaultForm() {
  return {
    title: '',
    summary: '',
    content: '',
    coverImage: '',
    categoryId: null,
    tagIds: [],
    status: 0,
    isTop: 0,
    isOriginal: 1,
    sourceUrl: '',
    authorId: null,
  }
}

const form = reactive(createDefaultForm())

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 200, message: '标题不能超过 200 字', trigger: 'blur' },
  ],
  summary: [{ max: 500, message: '摘要不能超过 500 字', trigger: 'blur' }],
  sourceUrl: [{ max: 255, message: '原文地址不能超过 255 字符', trigger: 'blur' }],
  content: [
    {
      required: true,
      trigger: 'change',
      validator: (rule, value, callback) => {
        // MarkdownEditor 通过 v-model 同步 Markdown 字符串到 form.content
        if (!value || !value.trim()) {
          callback(new Error('正文不能为空'))
        } else {
          callback()
        }
      },
    },
  ],
}

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已下架' },
]

onMounted(async () => {
  loading.value = true
  try {
    const tasks = [getCategoryTree(), getAllTags()]
    if (isEdit.value) tasks.push(getArticleDetail(articleId))
    const [categoryRes, tagRes, detailRes] = await Promise.all(tasks)

    categoryTree.value = categoryRes.data || []
    tagOptions.value = tagRes.data || []
    if (detailRes) {
      detail.value = detailRes.data
      applyDetail(detailRes.data)
    } else {
      // 新增：作者默认为当前登录用户（后端必填字段，不渲染成输入控件）
      form.authorId = authStore.user?.id ?? null
    }
  } catch {
    // 错误提示已由 request.js 统一弹出；详情加载失败时留在空表单即可
  } finally {
    loading.value = false
    snapshot()
  }
})

function applyDetail(data) {
  form.title = data.title ?? ''
  form.summary = data.summary ?? ''
  form.content = data.content ?? ''
  form.coverImage = data.coverImage ?? ''
  form.categoryId = data.categoryId ?? null
  form.tagIds = (data.tags || []).map((t) => t.id)
  form.status = data.status ?? 0
  form.isTop = data.isTop ?? 0
  form.isOriginal = data.isOriginal ?? 1
  form.sourceUrl = data.sourceUrl ?? ''
  form.authorId = data.authorId ?? authStore.user?.id ?? null
}

// ===== 未保存改动追踪 =====

let savedSnapshot = ''

function formState() {
  return {
    title: form.title,
    summary: form.summary,
    content: form.content,
    coverImage: form.coverImage,
    categoryId: form.categoryId,
    // 排序后比较，标签顺序变化不算脏
    tagIds: [...form.tagIds].sort((a, b) => a - b),
    status: form.status,
    isTop: form.isTop,
    isOriginal: form.isOriginal,
    sourceUrl: form.sourceUrl,
  }
}

function snapshot() {
  savedSnapshot = JSON.stringify(formState())
}

function isDirty() {
  return JSON.stringify(formState()) !== savedSnapshot
}

function handleBeforeUnload(e) {
  if (isDirty()) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload))
onUnmounted(() => window.removeEventListener('beforeunload', handleBeforeUnload))

onBeforeRouteLeave(() => {
  if (!isDirty() || submitting.value) return true
  return ElMessageBox.confirm('当前文章尚未保存，离开将丢失改动，是否继续？', '未保存提醒', {
    type: 'warning',
    confirmButtonText: '离开',
    cancelButtonText: '留在本页',
  })
    .then(() => true)
    .catch(() => false)
})

// ===== 提交 =====

async function handleSubmit(saveAsDraft = false) {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.authorId) {
    ElMessage.error('未能获取作者信息，请重新登录后再试')
    return
  }

  const payload = {
    title: form.title.trim(),
    content: form.content,
    // 空值传 null 而非 ''：后端 summary 为空时会自动截取正文前 100 字
    summary: form.summary.trim() || null,
    coverImage: form.coverImage.trim() || null,
    categoryId: form.categoryId ?? null,
    authorId: form.authorId,
    status: saveAsDraft ? 0 : form.status,
    isTop: form.isTop ?? 0,
    isOriginal: form.isOriginal,
    // 转载才提交原文地址，切回原创时清空
    sourceUrl: form.isOriginal === 0 ? form.sourceUrl.trim() || null : null,
    // 始终传完整数组：整体替换语义，没改标签时传回原数组无副作用
    tagIds: form.tagIds,
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateArticle(articleId, payload)
      ElMessage.success('保存成功')
      // 保存后留在页面继续编辑，刷新头部元信息
      const { data } = await getArticleDetail(articleId)
      detail.value = data
      snapshot()
    } else {
      const { data } = await createArticle(payload)
      ElMessage.success('新增成功')
      snapshot()
      // 跳到编辑态（携带新 id），后续保存走 PUT，避免重复新增
      router.replace(`/admin/article/edit/${data.id}`)
    }
  } catch {
    // 失败提示已由 request.js 统一弹出
  } finally {
    submitting.value = false
  }
}

function handleBack() {
  // 有未保存改动时由 onBeforeRouteLeave 统一拦截确认
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/article/edit')
  }
}
</script>

<template>
  <div v-loading="loading" class="edit-page">
    <header class="edit-header">
      <div class="edit-header-left">
        <el-button text :icon="Back" @click="handleBack">返回</el-button>
        <h1 class="edit-title">{{ isEdit ? '编辑文章' : '新增文章' }}</h1>
      </div>
      <span v-if="isEdit && detail" class="edit-meta">
        作者：{{ detail.authorName || detail.authorUsername }} ·
        最后更新：{{ formatDateTime(detail.updatedAt) }}
      </span>
    </header>

    <el-card class="edit-card" shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="200"
            show-word-limit
            placeholder="请输入文章标题"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                node-key="id"
                check-strictly
                clearable
                filterable
                placeholder="请选择分类（可不选）"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tagIds">
              <el-select
                v-model="form.tagIds"
                multiple
                filterable
                clearable
                placeholder="请选择标签（可不选）"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in tagOptions"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="留空将自动截取正文前 100 字"
          />
        </el-form-item>

        <el-form-item label="封面图 URL" prop="coverImage">
          <el-input
            v-model="form.coverImage"
            maxlength="255"
            placeholder="https://..."
            clearable
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类型" prop="isOriginal">
              <el-radio-group v-model="form.isOriginal">
                <el-radio :value="1">原创</el-radio>
                <el-radio :value="0">转载</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置顶" prop="isTop">
              <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.isOriginal === 0" label="原文地址" prop="sourceUrl">
          <el-input
            v-model="form.sourceUrl"
            maxlength="255"
            placeholder="https://..."
            clearable
          />
        </el-form-item>

        <el-form-item label="正文（Markdown）" prop="content">
          <MarkdownEditor v-model="form.content" />
        </el-form-item>
      </el-form>

      <div class="edit-actions">
        <el-button
          type="primary"
          :icon="Check"
          :loading="submitting"
          @click="handleSubmit(false)"
        >
          保存
        </el-button>
        <el-button :loading="submitting" @click="handleSubmit(true)">存为草稿</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.edit-page {
  max-width: 1000px;
  padding: 20px 24px 40px;
  margin: 0 auto;
}

.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.edit-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.edit-meta {
  font-size: 12px;
  color: #909399;
}

.edit-card {
  border-radius: 6px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
