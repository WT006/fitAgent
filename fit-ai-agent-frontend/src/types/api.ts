export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

export const API_CODE = {
  SUCCESS: 0,
  UNAUTHORIZED: 40100,
} as const
