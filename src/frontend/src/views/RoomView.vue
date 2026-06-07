<template>
  <div class="room-page">
    <el-header class="header">
      <div>
        <el-button text @click="$router.push('/')">← 返回</el-button>
        <span class="room-name">{{ room?.name }}</span>
        <el-tag size="small" style="margin-left:8px">{{ room?.roomCode }}</el-tag>
      </div>
    </el-header>
    <div class="room-body">
      <div class="chat-area">
        <div class="messages" ref="messagesRef">
          <div v-for="msg in messages" :key="msg.id" class="message-item">
            <span class="sender">[{{ msg.senderId }}]</span>
            <span class="content">{{ msg.content }}</span>
            <el-tag v-if="msg.type !== 'TEXT'" size="small" type="info" style="margin-left:4px">{{ msg.type }}</el-tag>
          </div>
        </div>
        <div class="input-area">
          <el-input v-model="inputText" placeholder="输入消息..." @keyup.enter="sendMessage" />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
      <div class="side-panel">
        <el-card>
          <template #header>成员</template>
          <div v-for="m in members" :key="m.userId" class="member-item">
            {{ m.userId }} ({{ m.role }})
          </div>
        </el-card>
        <el-card style="margin-top:12px">
          <template #header>掷骰</template>
          <el-input v-model="diceExpr" placeholder="如 2d6+3" @keyup.enter="rollDice" />
          <el-button type="success" @click="rollDice" style="margin-top:8px;width:100%">掷骰</el-button>
          <div v-if="diceResult" class="dice-result">结果: {{ diceResult }}</div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'

const route = useRoute()
const userStore = useUserStore()
const roomId = Number(route.params.roomId)
const room = ref(null)
const messages = ref([])
const members = ref([])
const inputText = ref('')
const diceExpr = ref('')
const diceResult = ref('')
const messagesRef = ref(null)
let pollTimer = null

onMounted(async () => {
  if (!userStore.user) {
    const { data } = await api.get('/users/me')
    userStore.user = data
  }
  await loadRoom()
  await loadMessages()
  pollTimer = setInterval(loadMessages, 3000)
})

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })

async function loadRoom() {
  const [roomRes, membersRes] = await Promise.all([
    api.get(`/rooms/${roomId}`),
    api.get(`/rooms/${roomId}/members`)
  ])
  room.value = roomRes.data
  members.value = membersRes.data
}

async function loadMessages() {
  const { data } = await api.get(`/rooms/${roomId}/messages`)
  messages.value = data
  await nextTick()
  const el = messagesRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function sendMessage() {
  if (!inputText.value.trim()) return
  await api.post(`/rooms/${roomId}/messages`, { content: inputText.value })
  inputText.value = ''
  await loadMessages()
}

async function rollDice() {
  if (!diceExpr.value.trim()) return
  try {
    const { data } = await api.post('/dice/roll', { expression: diceExpr.value })
    diceResult.value = `${diceExpr.value} = ${data.total}`
  } catch { /* handled by interceptor */ }
}
</script>

<style scoped>
.room-page { height: 100vh; display: flex; flex-direction: column; }
.header { background: #fff; display: flex; align-items: center; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.room-name { font-weight: 600; margin-left: 8px; }
.room-body { flex: 1; display: flex; overflow: hidden; padding: 12px; gap: 12px; }
.chat-area { flex: 1; display: flex; flex-direction: column; }
.messages { flex: 1; overflow-y: auto; padding: 12px; background: #fff; border-radius: 8px; }
.message-item { margin-bottom: 8px; }
.sender { color: #409eff; font-weight: 500; margin-right: 6px; }
.input-area { display: flex; gap: 8px; margin-top: 12px; }
.side-panel { width: 260px; }
.member-item { padding: 4px 0; font-size: 14px; }
.dice-result { margin-top: 8px; padding: 8px; background: #f0f9eb; border-radius: 4px; font-weight: 600; }
</style>