import request from './request'

export interface PdfDownloadVO {
  url: string
  fileName: string
  expiresInSeconds: number
}

/** 登录后获取 PDF 短时下载链接 */
export function getPdfDownloadUrl(downloadKey: string) {
  return request.get<unknown, PdfDownloadVO>('/files/pdf/url', {
    params: { downloadKey },
  })
}
