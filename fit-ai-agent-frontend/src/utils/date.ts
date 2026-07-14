import dayjs from 'dayjs'

export const DATE_FORMAT = 'YYYY-MM-DD'

export function formatDate(date: string | Date): string {
  return dayjs(date).format(DATE_FORMAT)
}

export function formatDisplayDate(date: string): string {
  return dayjs(date).format('M/D')
}

export function today(): string {
  return dayjs().format(DATE_FORMAT)
}

export function isToday(date: string): boolean {
  return dayjs(date).isSame(dayjs(), 'day')
}

export function addDays(date: string, days: number): string {
  return dayjs(date).add(days, 'day').format(DATE_FORMAT)
}

export function getDateRange(startDate: string, totalDays: number): string[] {
  return Array.from({ length: totalDays }, (_, i) => addDays(startDate, i))
}

export function isPast(date: string): boolean {
  return dayjs(date).isBefore(dayjs(), 'day')
}

export function isFuture(date: string): boolean {
  return dayjs(date).isAfter(dayjs(), 'day')
}
