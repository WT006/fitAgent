export interface LoginUserVO {
  /** 雪花 ID，以后端字符串序列化为准 */
  id: string
  userAccount: string
  userName: string
  userRole: string
  createTime: string
}

export interface LoginResult {
  token: string
  userVO: LoginUserVO
}

export interface RegisterForm {
  userAccount: string
  userPassword: string
  checkPassword: string
  userName?: string
}

export interface LoginForm {
  userAccount: string
  userPassword: string
}

/** 与 LoginUserVO 字段一致，用于管理端列表 */
export type UserVO = LoginUserVO

export interface UserQueryRequest {
  pageNum?: number
  pageSize?: number
  sortField?: string
  sortOrder?: 'ascend' | 'descend'
  id?: string | null
  userAccount?: string
  userName?: string
  userRole?: string
}

/** MyBatis-Flex Page 序列化结构 */
export interface PageResult<T> {
  pageNumber: number
  pageSize: number
  totalPage: number
  totalRow: number
  records: T[]
}
