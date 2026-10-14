<script setup>
import { formatDateTime } from '@/utils/format'

defineProps({
  article: { type: Object, required: true },
})
</script>

<template>
  <el-card class="article-card" shadow="hover">
    <div class="card-head">
      <router-link :to="`/post/${article.id}`" class="card-title">
        <el-tag v-if="article.isTop === 1" type="danger" size="small" effect="dark">置顶</el-tag>
        {{ article.title }}
      </router-link>
    </div>
    <p class="card-summary">{{ article.summary || '（暂无摘要）' }}</p>
    <div class="card-meta">
      <span>{{ article.authorName || article.authorUsername }}</span>
      <el-divider direction="vertical" />
      <span>{{ formatDateTime(article.publishTime || article.createdAt) }}</span>
      <el-divider direction="vertical" />
      <router-link v-if="article.categoryId" :to="`/category/${article.categoryId}`" class="card-link">
        {{ article.categoryName }}
      </router-link>
      <el-divider direction="vertical" />
      <span>浏览 {{ article.viewCount ?? 0 }}</span>
      <span>点赞 {{ article.likeCount ?? 0 }}</span>
      <span>评论 {{ article.commentCount ?? 0 }}</span>
    </div>
    <div v-if="article.tags?.length" class="card-tags">
      <router-link
        v-for="tag in article.tags"
        :key="tag.id"
        :to="`/tag/${tag.id}`"
        class="card-tag-link"
      >
        <el-tag size="small" type="info" effect="plain">{{ tag.name }}</el-tag>
      </router-link>
    </div>
  </el-card>
</template>

<style scoped>
.article-card {
  border-radius: 6px;
  cursor: default;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  text-decoration: none;
}

.card-title:hover {
  color: #409eff;
}

.card-summary {
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.7;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 0;
  font-size: 12px;
  color: #909399;
}

.card-link {
  color: #409eff;
  text-decoration: none;
}

.card-tags {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.card-tag-link {
  text-decoration: none;
}
</style>
