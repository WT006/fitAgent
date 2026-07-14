import request from './request'
import type {
  LoginForm,
  LoginResult,
  LoginUserVO,
  PageResult,
  RegisterForm,
  UserQueryRequest,
  UserVO,
} from '@/types/user'

export function register(data: RegisterForm) {
  return request.post<unknown, string>('/user/register', data)
}

export function login(data: LoginForm) {
  return request.post<unknown, LoginResult>('/user/login', data)
}

export function logout() {
  return request.post<unknown, boolean>('/user/logout')
}

export function getCurrentUser() {
  return request.get<unknown, LoginUserVO>('/user/get/login')
}

export function listUsersByPage(data: UserQueryRequest) {
  return request.post<unknown, PageResult<UserVO>>('/user/list/page', data)
}

export function deleteUser(id: string) {
  return request.post<unknown, boolean>('/user/delete', null, { params: { id } })
}
