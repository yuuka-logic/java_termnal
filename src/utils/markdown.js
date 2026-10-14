import MarkdownIt from 'markdown-it'

// html: false —— 不信任原始 HTML，防止 XSS；linkify 自动识别 URL
const md = new MarkdownIt({ html: false, linkify: true, breaks: false })

/** Markdown 原文 -> HTML（文章详情页展示用；编辑输入仍走 Tiptap） */
export function renderMarkdown(text) {
  return md.render(text || '')
}
