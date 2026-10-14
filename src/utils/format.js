/** 后端日期格式为 yyyy-MM-ddTHH:mm:ss（ISO-8601，无时区后缀），展示时把 T 换成空格 */
export function formatDateTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ')
}
