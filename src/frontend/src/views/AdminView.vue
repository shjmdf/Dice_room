<template>
  <div class="admin-page" v-loading="loading">
    <header class="topbar">
      <div>
        <div class="title">管理后台</div>
        <div class="subtitle">用户与房间状态管理</div>
      </div>
      <div class="actions">
        <el-button @click="load">刷新</el-button>
        <el-button @click="router.push('/')">返回首页</el-button>
      </div>
    </header>

    <main class="admin-shell">
      <section class="admin-section">
        <div class="section-title">用户</div>
        <el-table :data="users" stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="loginName" label="登录名" min-width="130" />
          <el-table-column prop="nickname" label="昵称" min-width="130" />
          <el-table-column prop="role" label="角色" width="100" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                @click.stop="openUserDetail(row)"
              >
                详情
              </el-button>
              <el-button
                size="small"
                :disabled="row.role === 'ADMIN' || row.status !== 'ACTIVE'"
                @click.stop="suspendUser(row)"
              >
                封禁
              </el-button>
              <el-button
                size="small"
                :disabled="row.status !== 'SUSPENDED'"
                @click.stop="recoverUser(row)"
              >
                恢复
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                :disabled="row.role === 'ADMIN' || row.status === 'DELETED'"
                @click.stop="deleteUser(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="admin-section">
        <div class="section-title">房间</div>
        <el-table :data="rooms" stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="房间名" min-width="160" />
          <el-table-column prop="roomCode" label="房间码" width="120" />
          <el-table-column prop="ownerId" label="房主 ID" width="100" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                :disabled="row.status !== 'OPEN'"
                @click.stop="closeRoom(row)"
              >
                关闭
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                :disabled="row.status === 'DELETED'"
                @click.stop="deleteRoom(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="admin-section">
        <div class="section-title with-tools">
          <span>邀请码</span>
          <div class="invite-form">
            <el-input-number v-model="newInvite.usageLimit" :min="1" :max="999" size="small" controls-position="right" />
            <el-date-picker
              v-model="newInvite.expirationDate"
              type="datetime"
              size="small"
              placeholder="过期时间"
              class="date-picker"
            />
            <el-button size="small" type="primary" :loading="creatingInvite" @click="createInviteCode">创建邀请码</el-button>
          </div>
        </div>
        <el-table :data="inviteCodes" stripe>
          <el-table-column prop="code" label="邀请码" min-width="150" />
          <el-table-column label="使用次数" width="120">
            <template #default="{ row }">{{ row.usedCount }} / {{ row.usageLimit }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column label="过期时间" min-width="180">
            <template #default="{ row }">{{ formatDate(row.expirationTimestampMillis) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                :disabled="row.status !== 'ACTIVE'"
                @click.stop="disableInviteCode(row)"
              >
                停止
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                @click.stop="deleteInviteCode(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </main>

    <el-dialog v-model="showUserDialog" title="用户详情" width="520">
      <div v-if="selectedUser" class="user-detail">
        <div class="detail-row"><span>ID</span><strong>{{ selectedUser.id }}</strong></div>
        <div class="detail-row"><span>登录名</span><strong>{{ selectedUser.loginName }}</strong></div>
        <div class="detail-row"><span>昵称</span><strong>{{ selectedUser.nickname }}</strong></div>
        <div class="detail-row"><span>角色</span><strong>{{ selectedUser.role }}</strong></div>
        <div class="detail-row"><span>状态</span><strong>{{ selectedUser.status }}</strong></div>
        <div class="detail-row"><span>邮箱</span><strong>{{ selectedUser.email || '-' }}</strong></div>
        <div class="detail-row"><span>简介</span><strong>{{ selectedUser.description || '-' }}</strong></div>
        <el-divider />
        <label class="reset-password">
          重置密码
          <el-input v-model="resetPasswordValue" type="password" show-password placeholder="输入新密码" />
        </label>
      </div>
      <template #footer>
        <el-button @click="showUserDialog = false">关闭</el-button>
        <el-button type="primary" :loading="resettingPassword" @click="resetUserPassword">保存密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const loading = ref(false)
const creatingInvite = ref(false)
const resettingPassword = ref(false)
const showUserDialog = ref(false)
const users = ref([])
const rooms = ref([])
const inviteCodes = ref([])
const selectedUser = ref(null)
const resetPasswordValue = ref('')
const newInvite = reactive({
  usageLimit: 1,
  expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [userRes, roomRes, inviteRes] = await Promise.all([
      api.get('/users'),
      api.get('/rooms/admin'),
      api.get('/invite-codes')
    ])
    users.value = userRes.data
    rooms.value = roomRes.data
    inviteCodes.value = inviteRes.data
  } finally {
    loading.value = false
  }
}

async function suspendUser(user) {
  await api.post(`/users/${user.id}/suspend`)
  ElMessage.success('用户已封禁')
  await load()
}

async function recoverUser(user) {
  await api.post(`/users/${user.id}/recover`)
  ElMessage.success('用户已恢复')
  await load()
}

async function deleteUser(user) {
  await ElMessageBox.confirm(`确定删除用户「${user.nickname}」吗？`, '删除用户', { type: 'warning' })
  await api.delete(`/users/${user.id}`)
  ElMessage.success('用户已删除')
  await load()
}

async function openUserDetail(user) {
  const { data } = await api.get(`/users/${user.id}`)
  selectedUser.value = data
  resetPasswordValue.value = ''
  showUserDialog.value = true
}

async function resetUserPassword() {
  if (!selectedUser.value) return
  if (!resetPasswordValue.value.trim()) {
    ElMessage.warning('请输入新密码')
    return
  }

  resettingPassword.value = true
  try {
    await api.patch(`/users/${selectedUser.value.id}/password`, { password: resetPasswordValue.value })
    resetPasswordValue.value = ''
    ElMessage.success('密码已重置')
  } finally {
    resettingPassword.value = false
  }
}

async function closeRoom(room) {
  await api.post(`/rooms/admin/${room.id}/close`)
  ElMessage.success('房间已关闭')
  await load()
}

async function deleteRoom(room) {
  await ElMessageBox.confirm(`确定删除房间「${room.name}」吗？`, '删除房间', { type: 'warning' })
  await api.delete(`/rooms/admin/${room.id}`)
  ElMessage.success('房间已删除')
  await load()
}

async function createInviteCode() {
  if (!newInvite.expirationDate) {
    ElMessage.warning('请选择过期时间')
    return
  }

  creatingInvite.value = true
  try {
    const { data } = await api.post('/invite-codes', {
      usageLimit: newInvite.usageLimit,
      expirationTimestampMillis: newInvite.expirationDate.getTime()
    })
    ElMessage.success(`邀请码已创建：${data.code}`)
    await load()
  } finally {
    creatingInvite.value = false
  }
}

async function disableInviteCode(inviteCode) {
  await api.post(`/invite-codes/${inviteCode.code}/disable`)
  ElMessage.success('邀请码已停止')
  await load()
}

async function deleteInviteCode(inviteCode) {
  await ElMessageBox.confirm(`确定删除邀请码「${inviteCode.code}」吗？`, '删除邀请码', { type: 'warning' })
  await api.delete(`/invite-codes/${inviteCode.code}`)
  ElMessage.success('邀请码已删除')
  await load()
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString()
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f4f6f8;
  color: #20252b;
}

.topbar {
  height: 64px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #d8dee6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 700;
}

.subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #687381;
}

.actions {
  display: flex;
  gap: 8px;
}

.admin-shell {
  max-width: 1180px;
  margin: 0 auto;
  padding: 20px;
}

.admin-section {
  background: #ffffff;
  border: 1px solid #d8dee6;
  border-radius: 8px;
  padding: 18px;
  margin-bottom: 16px;
}

.section-title {
  margin-bottom: 14px;
  font-weight: 700;
}

.with-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.invite-form {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.date-picker {
  width: 210px;
}

.user-detail {
  display: grid;
  gap: 10px;
}

.detail-row {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  gap: 12px;
  font-size: 13px;
}

.detail-row span,
.reset-password {
  color: #687381;
}

.detail-row strong {
  min-width: 0;
  color: #20252b;
  font-weight: 600;
  word-break: break-word;
}

.reset-password {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
}

@media (max-width: 720px) {
  .topbar {
    height: auto;
    min-height: 64px;
    gap: 12px;
    align-items: flex-start;
    flex-direction: column;
    padding: 14px 20px;
  }

  .with-tools {
    align-items: flex-start;
    flex-direction: column;
  }

  .invite-form,
  .date-picker {
    width: 100%;
  }
}
</style>
