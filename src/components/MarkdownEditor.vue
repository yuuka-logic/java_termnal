<script setup>
import { computed, watch } from 'vue'
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import { Markdown } from 'tiptap-markdown'

const props = defineProps({
  /** v-model：Markdown 字符串（后端 article.content 的原生存储格式） */
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入正文，支持 Markdown 语法…' },
})

const emit = defineEmits(['update:modelValue'])

const editor = useEditor({
  // tiptap-markdown 会把字符串内容按 Markdown 解析为富文本
  content: props.modelValue,
  extensions: [
    StarterKit,
    Placeholder.configure({ placeholder: props.placeholder }),
    Markdown.configure({
      html: false, // 不进不出 HTML，保持纯 Markdown
      breaks: false, // 单个换行不转 <br>，遵循标准 Markdown 语义
      transformPastedText: true, // 粘贴的 Markdown 文本自动解析为富文本
    }),
  ],
  onUpdate: ({ editor }) => {
    // 编辑器 -> 父组件：序列化回 Markdown
    emit('update:modelValue', editor.storage.markdown.getMarkdown())
  },
})

// 父组件 -> 编辑器：外部回填（编辑模式异步加载详情后）。
// 与当前 Markdown 不一致才 setContent，避免每次输入触发回写循环和光标跳动
watch(
  () => props.modelValue,
  (val) => {
    const ed = editor.value
    if (!ed) return
    if ((val || '') !== ed.storage.markdown.getMarkdown()) {
      // 第二个参数 false：不触发 onUpdate，防止 emit 循环
      ed.commands.setContent(val || '', false)
    }
  },
)

// 工具栏配置。active 在此求值为普通布尔值：useEditor 的 customRef
// 会在每次 transaction 后触发重算，按钮高亮得以实时刷新
const toolbar = computed(() => {
  const ed = editor.value
  if (!ed) return []
  const chain = () => ed.chain().focus()
  return [
    {
      items: [
        { label: '↶', title: '撤销', run: () => chain().undo().run() },
        { label: '↷', title: '重做', run: () => chain().redo().run() },
      ],
    },
    {
      items: [
        {
          label: 'H2',
          title: '二级标题',
          active: ed.isActive('heading', { level: 2 }),
          run: () => chain().toggleHeading({ level: 2 }).run(),
        },
        {
          label: 'H3',
          title: '三级标题',
          active: ed.isActive('heading', { level: 3 }),
          run: () => chain().toggleHeading({ level: 3 }).run(),
        },
      ],
    },
    {
      items: [
        {
          label: 'B',
          title: '粗体',
          cls: 't-bold',
          active: ed.isActive('bold'),
          run: () => chain().toggleBold().run(),
        },
        {
          label: 'I',
          title: '斜体',
          cls: 't-italic',
          active: ed.isActive('italic'),
          run: () => chain().toggleItalic().run(),
        },
        {
          label: 'S',
          title: '删除线',
          cls: 't-strike',
          active: ed.isActive('strike'),
          run: () => chain().toggleStrike().run(),
        },
        {
          label: '</>',
          title: '行内代码',
          cls: 't-code',
          active: ed.isActive('code'),
          run: () => chain().toggleCode().run(),
        },
      ],
    },
    {
      items: [
        {
          label: '❝',
          title: '引用',
          active: ed.isActive('blockquote'),
          run: () => chain().toggleBlockquote().run(),
        },
        {
          label: '{ }',
          title: '代码块',
          cls: 't-code',
          active: ed.isActive('codeBlock'),
          run: () => chain().toggleCodeBlock().run(),
        },
      ],
    },
    {
      items: [
        {
          label: '• 列表',
          title: '无序列表',
          active: ed.isActive('bulletList'),
          run: () => chain().toggleBulletList().run(),
        },
        {
          label: '1. 列表',
          title: '有序列表',
          active: ed.isActive('orderedList'),
          run: () => chain().toggleOrderedList().run(),
        },
        {
          label: '分隔线',
          title: '插入分隔线',
          run: () => chain().setHorizontalRule().run(),
        },
      ],
    },
    {
      items: [
        {
          label: '清除格式',
          title: '清除格式',
          run: () => chain().clearNodes().unsetAllMarks().run(),
        },
      ],
    },
  ]
})

function isEmpty() {
  return editor.value ? editor.value.isEmpty : true
}

defineExpose({ isEmpty })
</script>

<template>
  <div class="md-editor">
    <div v-if="editor" class="md-toolbar">
      <div v-for="(group, gi) in toolbar" :key="gi" class="md-toolbar-group">
        <button
          v-for="item in group.items"
          :key="item.title"
          type="button"
          class="md-btn"
          :class="[item.cls, { active: item.active }]"
          :title="item.title"
          @mousedown.prevent
          @click="item.run()"
        >
          {{ item.label }}
        </button>
      </div>
    </div>
    <EditorContent :editor="editor" class="md-content" />
  </div>
</template>

<style scoped>
.md-editor {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.2s;
}

.md-editor:focus-within {
  border-color: #409eff;
}

.md-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 6px 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  border-radius: 4px 4px 0 0;
}

.md-toolbar-group {
  display: flex;
  gap: 2px;
  padding-right: 8px;
  border-right: 1px solid #dcdfe6;
}

.md-toolbar-group:last-child {
  padding-right: 0;
  border-right: none;
}

.md-btn {
  min-width: 28px;
  height: 26px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 1;
  color: #606266;
  background: transparent;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.md-btn:hover {
  color: #303133;
  background: #e8eaef;
}

.md-btn.active {
  color: #409eff;
  background: #d9ecff;
}

.t-bold {
  font-weight: 700;
}

.t-italic {
  font-style: italic;
}

.t-strike {
  text-decoration: line-through;
}

.t-code {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 11px;
}

.md-content :deep(.ProseMirror) {
  min-height: 420px;
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.75;
  outline: none;
  word-break: break-word;
}

/* 空文档占位符（@tiptap/extension-placeholder 官方样式） */
.md-content :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  height: 0;
  color: #c0c4cc;
  pointer-events: none;
}

/* 正文基础排版：Tiptap v2 不注入默认样式，这里补齐 */
.md-content :deep(.ProseMirror h1),
.md-content :deep(.ProseMirror h2),
.md-content :deep(.ProseMirror h3) {
  margin: 0.8em 0 0.4em;
  font-weight: 600;
  line-height: 1.4;
}

.md-content :deep(.ProseMirror h1) {
  font-size: 1.6em;
}

.md-content :deep(.ProseMirror h2) {
  font-size: 1.4em;
}

.md-content :deep(.ProseMirror h3) {
  font-size: 1.2em;
}

.md-content :deep(.ProseMirror p) {
  margin: 0.4em 0;
}

.md-content :deep(.ProseMirror ul),
.md-content :deep(.ProseMirror ol) {
  margin: 0.4em 0;
  padding-left: 1.6em;
}

.md-content :deep(.ProseMirror blockquote) {
  margin: 0.6em 0;
  padding: 2px 0 2px 12px;
  border-left: 3px solid #dcdfe6;
  color: #909399;
}

.md-content :deep(.ProseMirror pre) {
  margin: 0.6em 0;
  padding: 10px 14px;
  overflow-x: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #abb2bf;
  background: #282c34;
  border-radius: 4px;
}

.md-content :deep(.ProseMirror :not(pre) > code) {
  padding: 2px 5px;
  font-family: Consolas, Monaco, monospace;
  font-size: 0.9em;
  color: #c7254e;
  background: #f9f2f4;
  border-radius: 3px;
}

.md-content :deep(.ProseMirror hr) {
  margin: 1em 0;
  border: none;
  border-top: 1px solid #dcdfe6;
}

.md-content :deep(.ProseMirror a) {
  color: #409eff;
}
</style>
