# personal_blog 个人博客管理后台（前端）

Vue 3 + Vite + Element Plus + Pinia + Vue Router，文章正文使用 Tiptap（`@tiptap/vue-3` v2 + `tiptap-markdown`）编辑，`v-model` 直接收发 Markdown 字符串。

后端为 Spring Boot 项目（`../myblog`，端口 8080），接口契约见后端根目录的《personal_blog后端API文档.docx》。

## 开发

```bash
npm install
npm run dev    # http://localhost:5173，/api 已代理到 http://localhost:8080
```

默认测试账号：`admin / 123456`（token 有效期 2 小时，后端重启需重新登录）。

## 结构

- `src/api/` — 接口封装，按模块分文件，统一走 `request.js`（自动带 `token` 请求头、解包 `{ code, message, data }`、判断 `code === 200` 而非 HTTP status）
- `src/components/MarkdownEditor.vue` — Tiptap 编辑器封装（工具栏 + Markdown 双向转换）
- `src/views/article-edit.vue` — 文章编辑页（`/article/edit/:id?`，无 id 为新增，有 id 为编辑）
- `src/views/login.vue` — 登录页
- `src/stores/auth.js` — 登录态（token + user，localStorage 持久化）
- `src/router/index.js` — 路由与登录守卫（`meta.requiresAuth`）

## 约定

- Composition API + `<script setup>`；页面文件 kebab-case，组件文件 PascalCase
- 后端响应 HTTP 恒为 200，业务状态码在 `body.code`；写操作需 `token` 请求头（不是 Authorization）
- 日期格式 `yyyy-MM-ddTHH:mm:ss`，展示统一用 `formatDateTime()`（把 T 换成空格）
- 文章 `tagIds` 语义：不传 = 不动标签，`[]` = 清空，数组 = 整体替换

## 生产部署

`npm run build` 产出 `dist/`；生产环境需由 nginx 等把 `/api` 转发到后端（与 `vite.config.js` 中代理规则一致）。
