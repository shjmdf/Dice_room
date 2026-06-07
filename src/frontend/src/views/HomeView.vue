<template>
  <div class="home-page">
    <el-header class="header">
      <span class="welcome">欢迎, {{ userStore.user?.nickname }}</span>
      <div class="header-actions">
        <el-button text @click="router.push('/profile')">个人资料</el-button>
        <el-button v-if="userStore.user?.role === 'ADMIN'" text @click="router.push('/admin')">管理后台</el-button>
        <el-button text @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    <el-row :gutter="20" style="padding: 20px;">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的房间</span>
              <div>
                <el-button type="primary" size="small" @click="showCreate = true">创建房间</el-button>
                <el-button size="small" @click="showJoin = true">加入房间</el-button>
              </div>
            </div>
          </template>
          <el-table :data="rooms" stripe @row-click="enterRoom">
            <el-table-column prop="name" label="房间名" />
            <el-table-column prop="roomCode" label="房间码" width="120" />
            <el-table-column prop="status" label="状态" width="80" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的角色卡</span>
              <el-button type="primary" size="small" @click="showNewCard = true">新建</el-button>
            </div>
          </template>
          <el-table :data="cards" stripe @row-click="openCard">
            <el-table-column prop="name" label="角色名" />
            <el-table-column prop="era" label="时代" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showCreate" title="创建房间" width="400">
      <el-input v-model="newRoomName" placeholder="房间名称" />
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="createRoom">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showJoin" title="加入房间" width="400">
      <el-input v-model="joinCode" placeholder="输入房间码" />
      <template #footer>
        <el-button @click="showJoin = false">取消</el-button>
        <el-button type="primary" @click="joinRoom">加入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showNewCard" title="新建角色卡" width="400">
      <el-input v-model="newCardName" placeholder="角色名" />
      <template #footer>
        <el-button @click="showNewCard = false">取消</el-button>
        <el-button type="primary" @click="createCard">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()
const rooms = ref([])
const cards = ref([])
const showCreate = ref(false)
const showJoin = ref(false)
const showNewCard = ref(false)
const newRoomName = ref('')
const joinCode = ref('')
const newCardName = ref('')

onMounted(async () => {
  if (!userStore.user) {
    const { data } = await api.get('/users/me')
    userStore.user = data
  }
  await loadData()
})

async function loadData() {
  const userId = userStore.user.id
  const [roomRes, cardRes] = await Promise.all([
    api.get(`/rooms/user/${userId}`),
    api.get(`/player-cards/user/${userId}`)
  ])
  rooms.value = roomRes.data
  cards.value = cardRes.data
}

async function createRoom() {
  await api.post('/rooms', { name: newRoomName.value })
  showCreate.value = false
  newRoomName.value = ''
  await loadData()
}

async function joinRoom() {
  await api.post('/rooms/join', { roomCode: joinCode.value })
  showJoin.value = false
  joinCode.value = ''
  await loadData()
}

async function createCard() {
  const { data } = await api.post('/player-cards', { name: newCardName.value })
  showNewCard.value = false
  newCardName.value = ''
  router.push(`/card/${data.id}`)
}

function enterRoom(row) { router.push(`/room/${row.id}`) }
function openCard(row) { router.push(`/card/${row.id}`) }

function handleLogout() {
  api.post('/users/logout').catch(() => {})
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home-page { min-height: 100vh; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.welcome { font-weight: 600; }
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
