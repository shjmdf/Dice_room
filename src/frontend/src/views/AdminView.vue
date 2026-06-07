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
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
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
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const loading = ref(false)
const users = ref([])
const rooms = ref([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [userRes, roomRes] = await Promise.all([
      api.get('/users'),
      api.get('/rooms/admin')
    ])
    users.value = userRes.data
    rooms.value = roomRes.data
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

@media (max-width: 720px) {
  .topbar {
    height: auto;
    min-height: 64px;
    gap: 12px;
    align-items: flex-start;
    flex-direction: column;
    padding: 14px 20px;
  }
}
</style>
