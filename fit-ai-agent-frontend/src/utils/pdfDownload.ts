export interface PdfDownloadInfo {
  downloadKey: string
  displayName: string
}

/**
 * 从 Manus 步骤/消息文本中解析 PDF 下载信息
 */
export function parsePdfDownloadInfo(text: string): PdfDownloadInfo | null {
  if (!text) return null
  const keyMatch = text.match(/downloadKey=([^\s]+)/)
  if (!keyMatch) return null
  const downloadKey = keyMatch[1].trim()
  if (!downloadKey.toLowerCase().endsWith('.pdf')) return null

  const nameMatch = text.match(/displayName=([^\s]+)/)
  let displayName = nameMatch?.[1]?.trim()
  if (!displayName) {
    const raw = downloadKey.split('/').pop() || 'plan.pdf'
    const idx = raw.indexOf('_')
    displayName = idx > 0 ? raw.slice(idx + 1) : raw
  }
  return { downloadKey, displayName }
}

export function collectPdfDownloadsFromMessage(content: string, steps?: Array<{ content: string }>): PdfDownloadInfo[] {
  const texts = [content, ...(steps?.map((s) => s.content) ?? [])].filter(Boolean)
  const map = new Map<string, PdfDownloadInfo>()
  for (const text of texts) {
    const info = parsePdfDownloadInfo(text)
    if (info) {
      map.set(info.downloadKey, info)
    }
  }
  return [...map.values()]
}
