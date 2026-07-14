<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ChevronLeft, ChevronRight, Loader2, Search, Trash2, Users } from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import * as userApi from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import type { UserVO } from '@/types/user'

const auth = useAuthStore()
const { error: showError, success } = useToast()

const loading = ref(false)
const deleting = ref(false)
const records = ref<UserVO[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const totalRow = ref(0)
const totalPage = ref(0)

const pendingDelete = ref<UserVO | null>(null)

const filters = reactive({
  userAccount: '',
  userName: '',
  userRole: '',
})

const hasResults = computed(() => records.value.length > 0)
const canPrev = computed(() => pageNum.value > 1)
const canNext = computed(() => pageNum.value < totalPage.value)

async function fetchUsers(resetPage = false) {
  if (resetPage) pageNum.value = 1
  loading.value = true
  try {
    const data = await userApi.listUsersByPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userAccount: filters.userAccount.trim() || undefined,
      userName: filters.userName.trim() || undefined,
      userRole: filters.userRole || undefined,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    records.value = data.records ?? []
    totalRow.value = data.totalRow ?? 0
    totalPage.value = data.totalPage ?? 0
    pageNum.value = data.pageNumber ?? pageNum.value
  } catch (e) {
    showError(e instanceof Error ? e.message : '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchUsers(true)
}

function handleReset() {
  filters.userAccount = ''
  filters.userName = ''
  filters.userRole = ''
  fetchUsers(true)
}

function goPrev() {
  if (!canPrev.value) return
  pageNum.value -= 1
  fetchUsers()
}

function goNext() {
  if (!canNext.value) return
  pageNum.value += 1
  fetchUsers()
}

function roleLabel(role: string) {
  return role === 'admin' ? '管理员' : '用户'
}

function formatTime(value: string) {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 19)
}

/** 当前页内的连续序号（跨页延续） */
function rowIndex(index: number) {
  return (pageNum.value - 1) * pageSize.value + index + 1
}

function canDelete(user: UserVO) {
  return user.id !== auth.user?.id
}

function askDelete(user: UserVO) {
  if (!canDelete(user)) {
    showError('不能删除自己的账号')
    return
  }
  pendingDelete.value = user
}

function cancelDelete() {
  pendingDelete.value = null
}

async function confirmDelete() {
  const user = pendingDelete.value
  if (!user) return
  pendingDelete.value = null
  deleting.value = true
  try {
    await userApi.deleteUser(user.id)
    success('已删除用户')
    if (records.value.length <= 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    await fetchUsers()
  } catch (e) {
    showError(e instanceof Error ? e.message : '删除失败')
  } finally {
    deleting.value = false
  }
}

onMounted(() => fetchUsers())
</script>

<template>
  <AppLayout wide>
    <div class="space-y-5">
      <div class="flex items-start gap-3">
        <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <Users class="h-5 w-5" />
        </div>
        <div>
          <h1 class="font-heading text-xl font-semibold text-text">用户管理</h1>
          <p class="mt-1 text-sm text-text-muted">按账号、昵称或角色筛选系统用户</p>
        </div>
      </div>

      <form class="card p-4 space-y-3 sm:space-y-0 sm:flex sm:flex-wrap sm:items-end sm:gap-3" @submit.prevent="handleSearch">
        <div class="flex-1 min-w-[140px]">
          <label class="mb-1.5 block text-xs font-medium text-text-secondary" for="filter-account">账号</label>
          <input
            id="filter-account"
            v-model="filters.userAccount"
            type="text"
            class="input-field py-2.5"
            placeholder="模糊搜索账号"
          />
        </div>
        <div class="flex-1 min-w-[140px]">
          <label class="mb-1.5 block text-xs font-medium text-text-secondary" for="filter-name">昵称</label>
          <input
            id="filter-name"
            v-model="filters.userName"
            type="text"
            class="input-field py-2.5"
            placeholder="模糊搜索昵称"
          />
        </div>
        <div class="w-full sm:w-36">
          <label class="mb-1.5 block text-xs font-medium text-text-secondary" for="filter-role">角色</label>
          <select id="filter-role" v-model="filters.userRole" class="input-field py-2.5">
            <option value="">全部</option>
            <option value="user">用户</option>
            <option value="admin">管理员</option>
          </select>
        </div>
        <div class="flex gap-2">
          <button type="submit" class="btn-primary py-2.5" :disabled="loading">
            <Search class="h-4 w-4" />
            查询
          </button>
          <button type="button" class="btn-secondary py-2.5" :disabled="loading" @click="handleReset">
            重置
          </button>
        </div>
      </form>

      <div class="card overflow-hidden">
        <div class="flex items-center justify-between border-b border-border/60 px-4 py-3">
          <span class="text-sm text-text-muted">共 {{ totalRow }} 位用户</span>
          <Loader2 v-if="loading" class="h-4 w-4 animate-spin text-primary" />
        </div>

        <!-- Desktop table -->
        <div class="hidden sm:block overflow-x-auto">
          <table class="w-full text-left text-sm">
            <thead>
              <tr class="border-b border-border/60 bg-background-alt/60 text-text-secondary">
                <th class="px-4 py-3 font-medium w-14">序号</th>
                <th class="px-4 py-3 font-medium">账号</th>
                <th class="px-4 py-3 font-medium">昵称</th>
                <th class="px-4 py-3 font-medium">角色</th>
                <th class="px-4 py-3 font-medium">注册时间</th>
                <th class="px-4 py-3 font-medium text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!loading && !hasResults">
                <td colspan="6" class="px-4 py-12 text-center text-text-muted">暂无用户数据</td>
              </tr>
              <tr
                v-for="(user, index) in records"
                :key="user.id"
                class="border-b border-border/40 last:border-0 transition-colors duration-150 hover:bg-background-alt/50"
              >
                <td class="px-4 py-3 text-text-muted">{{ rowIndex(index) }}</td>
                <td class="px-4 py-3 font-medium text-text">{{ user.userAccount }}</td>
                <td class="px-4 py-3 text-text-secondary">{{ user.userName || '—' }}</td>
                <td class="px-4 py-3">
                  <span
                    class="inline-flex rounded-lg px-2 py-0.5 text-xs font-medium"
                    :class="
                      user.userRole === 'admin'
                        ? 'bg-teal/15 text-teal'
                        : 'bg-primary/10 text-primary'
                    "
                  >
                    {{ roleLabel(user.userRole) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-text-muted whitespace-nowrap">{{ formatTime(user.createTime) }}</td>
                <td class="px-4 py-3 text-right">
                  <button
                    v-if="canDelete(user)"
                    type="button"
                    class="inline-flex items-center gap-1 rounded-lg px-2.5 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 transition-colors cursor-pointer disabled:opacity-50"
                    :disabled="deleting"
                    @click="askDelete(user)"
                  >
                    <Trash2 class="h-3.5 w-3.5" />
                    删除
                  </button>
                  <span v-else class="text-xs text-text-muted">当前账号</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mobile cards -->
        <div class="sm:hidden divide-y divide-border/40">
          <div v-if="!loading && !hasResults" class="px-4 py-12 text-center text-sm text-text-muted">
            暂无用户数据
          </div>
          <div v-for="user in records" :key="user.id" class="px-4 py-3 space-y-2">
            <div class="flex items-center justify-between gap-2">
              <span class="font-medium text-text">{{ user.userAccount }}</span>
              <span
                class="inline-flex rounded-lg px-2 py-0.5 text-xs font-medium"
                :class="
                  user.userRole === 'admin'
                    ? 'bg-teal/15 text-teal'
                    : 'bg-primary/10 text-primary'
                "
              >
                {{ roleLabel(user.userRole) }}
              </span>
            </div>
            <p class="text-sm text-text-secondary">{{ user.userName || '未设置昵称' }}</p>
            <div class="flex items-center justify-between gap-2">
              <p class="text-xs text-text-muted">{{ formatTime(user.createTime) }}</p>
              <button
                v-if="canDelete(user)"
                type="button"
                class="inline-flex items-center gap-1 rounded-lg px-2.5 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 transition-colors cursor-pointer disabled:opacity-50"
                :disabled="deleting"
                @click="askDelete(user)"
              >
                <Trash2 class="h-3.5 w-3.5" />
                删除
              </button>
            </div>
          </div>
        </div>

        <div
          v-if="totalPage > 0"
          class="flex items-center justify-between border-t border-border/60 px-4 py-3"
        >
          <span class="text-xs text-text-muted">
            第 {{ pageNum }} / {{ totalPage }} 页
          </span>
          <div class="flex gap-2">
            <button
              type="button"
              class="btn-secondary px-3 py-1.5 text-xs"
              :disabled="!canPrev || loading"
              @click="goPrev"
            >
              <ChevronLeft class="h-3.5 w-3.5" />
              上一页
            </button>
            <button
              type="button"
              class="btn-secondary px-3 py-1.5 text-xs"
              :disabled="!canNext || loading"
              @click="goNext"
            >
              下一页
              <ChevronRight class="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <ConfirmDialog
      v-if="pendingDelete"
      title="删除用户"
      :message="`确认删除账号「${pendingDelete.userAccount}」？删除后该用户将无法登录。`"
      confirm-text="删除"
      destructive
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </AppLayout>
</template>
