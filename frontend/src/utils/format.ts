/**
 * 相对时间格式化
 */
export function formatRelativeTime(dateStr: string): string {
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

/**
 * 邀请码模糊预览（与后端逻辑保持一致）
 */
export function generatePreview(code: string): string {
  const len = code.length
  if (len <= 4) return '*'.repeat(len)
  const showLen = len <= 8 ? 2 : 3
  return code.substring(0, showLen) + '*'.repeat(len - showLen)
}

/**
 * 积分变化格式化（带+/-符号）
 */
export function formatDelta(delta: number): string {
  return delta > 0 ? `+${delta}` : `${delta}`
}
