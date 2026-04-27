export function normalizeDate(input) {
  if (!input) return null
  if (input instanceof Date) return input
  const normalized = typeof input === 'string' ? input.replace(' ', 'T') : input
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatTime(date) {
  if (!date) return ''
  const d = normalizeDate(date)
  if (!d) return ''
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 2 * day) return '昨天'
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  const y = d.getFullYear()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const dd = `${d.getDate()}`.padStart(2, '0')
  if (y === now.getFullYear()) return `${m}-${dd}`
  return `${y}-${m}-${dd}`
}

export function getRelativeTime(dateStr) {
  if (!dateStr) return ''
  const timestamp = typeof dateStr === 'number' ? dateStr : Date.parse(normalizeDate(dateStr))
  if (Number.isNaN(timestamp)) return ''
  return formatTime(new Date(timestamp))
}

export function getDateKey(input) {
  const date = normalizeDate(input)
  if (!date) return ''
  const y = date.getFullYear()
  const m = `${date.getMonth() + 1}`.padStart(2, '0')
  const d = `${date.getDate()}`.padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function getTimeText(input) {
  const date = normalizeDate(input)
  if (!date) return ''
  return `${`${date.getHours()}`.padStart(2, '0')}:${`${date.getMinutes()}`.padStart(2, '0')}`
}

export function formatRecordDate(dateString) {
  const date = normalizeDate(dateString)
  if (!date) return dateString
  const todayKey = getDateKey(new Date())
  const yesterdayKey = getDateKey(new Date(Date.now() - 86400000))
  const key = getDateKey(date)
  if (key === todayKey) return '今天'
  if (key === yesterdayKey) return '昨天'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

export function getAgeText(birthday) {
  const date = normalizeDate(birthday)
  if (!date) return ''
  const now = new Date()
  let years = now.getFullYear() - date.getFullYear()
  let months = now.getMonth() - date.getMonth()
  if (now.getDate() < date.getDate()) months -= 1
  if (months < 0) { years -= 1; months += 12 }
  return years > 0 ? `${years}岁` : `${Math.max(months, 1)}个月`
}
