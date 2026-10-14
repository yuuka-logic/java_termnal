<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <!-- key 绑定 path：新增保存成功后 replace 到 /admin/article/edit/:id 时强制重挂载；
       翻页等 query 变化不重挂载 -->
  <router-view :key="route.path" />
</template>
