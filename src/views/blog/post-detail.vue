<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { addArticleView, getArticleDetail, likeArticle } from '@/api/article'
import { renderMarkdown } from '@/utils/markdown'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const articleId = Number(route.params.id)

const loading = ref(true)
const detail = ref(null)
const likeSubmitting = ref(false)

const LIKED_KEY = 'pb_liked_articles'

// 本地点赞标记：后端接口无"是否已赞"查询，用 localStorage 记忆当前浏览器的点赞状态
const liked = ref(false)

const contentHtml = computed(() => renderMarkdown(detail.value?.content))

function readLikedSet() {
  try {
    return new Set(JSON.parse(localStorage.getItem(LIKED_KEY) || '[]'))
  } catch {
    return new Set()
  }
}

function writeLikedSet(set) {
  localStorage.setItem(LIKED_KEY, JSON.stringify([...set]))
}

async function toggleLike() {
  if (!detail.value || likeSubmitting.value) return
  likeSubmitting.value = true
  const next = !liked.value
  try {
    await likeArticle(articleId, next)
    liked.value = next
    const set = readLikedSet()
    if (next) set.add(articleId)
    else set.delete(articleId)
    writeLikedSet(set)
    detail.value.likeCount = Math.max(0, (detail.value.likeCount ?? 0) + (next ? 1 : -1))
  } finally {
    likeSubmitting.value = false
  }
}

onMounted(async () => {
  liked.value = readLikedSet().has(articleId)
  try {
    const { data } = await getArticleDetail(articleId)
    detail.value = data
  } finally {
    loading.value = false
  }
  // 浏览量 +1：不阻塞渲染，失败静默
  addArticleView(articleId).catch(() => {})
})
</script>

<template>
  <div v-loading="loading" class="post-page">
    <el-card v-if="detail" shadow="never" class="post-card">
      <h1 class="post-title">
        <el-tag v-if="detail.isTop === 1" type="danger" size="small" effect="dark">置顶</el-tag>
        {{ detail.title }}
      </h1>

      <div class="post-meta">
        <span>{{ detail.authorName || detail.authorUsername }}</span>
        <el-divider direction="vertical" />
        <span>发布于 {{ formatDateTime(detail.publishTime || detail.createdAt) }}</span>
        <el-divider direction="vertical" />
        <router-link v-if="detail.categoryId" :to="`/category/${detail.categoryId}`" class="post-link">
          {{ detail.categoryName }}
        </router-link>
        <el-divider direction="vertical" />
        <span>浏览 {{ detail.viewCount ?? 0 }}</span>
        <span>点赞 {{ detail.likeCount ?? 0 }}</span>
        <span>评论 {{ detail.commentCount ?? 0 }}</span>
      </div>

      <div v-if="detail.tags?.length" class="post-tags">
        <router-link v-for="tag in detail.tags" :key="tag.id" :to="`/tag/${tag.id}`" class="post-tag-link">
          <el-tag size="small" effect="plain">{{ tag.name }}</el-tag>
        </router-link>
      </div>

      <!-- 转载文章显示原文地址 -->
      <el-alert
        v-if="detail.isOriginal === 0 && detail.sourceUrl"
        :title="`本文转载自：${detail.sourceUrl}`"
        type="info"
        :closable="false"
        class="post-source"
      />

      <!-- 正文：Markdown 渲染（markdown-it html:false，防 XSS） -->
      <div class="md-body post-content" v-html="contentHtml" />

      <div class="post-like">
        <el-button
          :type="liked ? 'primary' : 'default'"
          :loading="likeSubmitting"
          round
          @click="toggleLike"
        >
          {{ liked ? '已点赞' : '点赞' }} · {{ detail.likeCount ?? 0 }}
        </el-button>
      </div>
    </el-card>
    <el-empty v-else-if="!loading" description="文章不存在或已被删除" />
  </div>
</template>

<style scoped>
.post-page {
  max-width: 820px;
  margin: 0 auto;
}

.post-card {
  border-radius: 6px;
}

.post-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 24px;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  font-size: 13px;
  color: #909399;
}

.post-link {
  color: #409eff;
  text-decoration: none;
}

.post-tags {
  display: flex;
  gap: 8px;
  margin: 14px 0 0;
}

.post-tag-link {
  text-decoration: none;
}

.post-source {
  margin-top: 16px;
}

.post-content {
  margin-top: 20px;
  padding-top: 4px;
}

.post-like {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;
}

/* 正文排版：与编辑器一致的 Markdown 展示样式 */
.md-body {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;
}

.md-body :deep(h1),
.md-body :deep(h2),
.md-body :deep(h3),
.md-body :deep(h4) {
  margin: 1em 0 0.5em;
  font-weight: 600;
  line-height: 1.4;
}

.md-body :deep(h1) {
  font-size: 1.6em;
}

.md-body :deep(h2) {
  font-size: 1.4em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #f0f2f5;
}

.md-body :deep(h3) {
  font-size: 1.2em;
}

.md-body :deep(p) {
  margin: 0.6em 0;
}

.md-body :deep(ul),
.md-body :deep(ol) {
  margin: 0.6em 0;
  padding-left: 1.8em;
}

.md-body :deep(blockquote) {
  margin: 0.8em 0;
  padding: 4px 0 4px 14px;
  border-left: 3px solid #dcdfe6;
  color: #909399;
}

.md-body :deep(pre) {
  margin: 0.8em 0;
  padding: 12px 16px;
  overflow-x: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #abb2bf;
  background: #282c34;
  border-radius: 6px;
}

.md-body :deep(:not(pre) > code) {
  padding: 2px 5px;
  font-family: Consolas, Monaco, monospace;
  font-size: 0.9em;
  color: #c7254e;
  background: #f9f2f4;
  border-radius: 3px;
}

.md-body :deep(hr) {
  margin: 1.2em 0;
  border: none;
  border-top: 1px solid #ebeef5;
}

.md-body :deep(a) {
  color: #409eff;
}

.md-body :deep(img) {
  max-width: 100%;
}
</style>
