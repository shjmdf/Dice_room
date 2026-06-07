<template>
  <div class="room-page" v-loading="loading">
    <header class="room-header">
      <div class="room-title-group">
        <el-button text class="back-button" @click="router.push('/')">返回</el-button>
        <div>
          <div class="room-title">{{ room?.name || '房间' }}</div>
          <div class="room-subtitle">
            <span>{{ room?.roomCode || '-' }}</span>
            <span>{{ room?.status || '' }}</span>
          </div>
        </div>
      </div>
      <div class="room-actions">
        <el-button size="small" @click="refreshAll">刷新</el-button>
        <el-button size="small" type="danger" plain @click="leaveRoom">离开</el-button>
      </div>
    </header>

    <main class="room-shell">
      <section class="chat-panel">
        <div ref="messagesRef" class="message-list">
          <div v-if="messages.length === 0" class="empty-state">暂无消息</div>
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-row"
            :class="{ mine: message.senderId === myId, system: message.type === 'SYSTEM' }"
          >
            <div v-if="message.type === 'SYSTEM'" class="system-message">
              {{ message.content }}
            </div>
            <template v-else>
              <div class="avatar">{{ avatarText(message.senderId) }}</div>
              <article class="message-bubble" :class="message.type.toLowerCase()">
                <div class="message-meta">
                  <span>{{ displayName(message.senderId) }}</span>
                  <span>{{ formatTime(message.timestamp) }}</span>
                </div>
                <div class="message-content">{{ message.content }}</div>
              </article>
            </template>
          </div>
        </div>

        <div class="composer">
          <el-radio-group v-model="sendMode" size="small" class="mode-tabs">
            <el-radio-button label="TEXT">消息</el-radio-button>
            <el-radio-button label="DICE">掷骰</el-radio-button>
            <el-radio-button label="CHECK">检定</el-radio-button>
          </el-radio-group>

          <div v-if="sendMode === 'CHECK'" class="check-row">
            <el-select v-model="activeCardId" placeholder="角色卡" class="card-select">
              <el-option v-for="card in myCards" :key="card.id" :label="card.name" :value="card.id" />
            </el-select>
            <el-input v-model="skillName" placeholder="技能名，例如：侦查" @keyup.enter="send" />
          </div>

          <div class="input-row">
            <el-input
              v-model="inputText"
              :placeholder="placeholder"
              resize="none"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              @keyup.ctrl.enter="send"
            />
            <el-button type="primary" class="send-button" :loading="sending" @click="send">发送</el-button>
          </div>
        </div>
      </section>

      <aside class="side-panel">
        <section class="side-section">
          <div class="section-head">
            <span>我的角色卡</span>
            <el-button text size="small" @click="router.push('/')">管理</el-button>
          </div>
          <div class="room-nickname-row">
            <el-input v-model="roomNickname" placeholder="房间内昵称" size="small" />
            <el-button size="small" :loading="savingNickname" @click="saveRoomNickname">保存</el-button>
          </div>
          <el-select
            v-model="activeCardId"
            placeholder="绑定到本房间"
            class="wide"
            clearable
            @change="bindCard"
          >
            <el-option v-for="card in myCards" :key="card.id" :label="card.name" :value="card.id" />
          </el-select>
          <div v-if="activeCard" class="card-summary">
            <div class="card-name">{{ activeCard.name }}</div>
            <el-button size="small" @click="router.push(`/card/${activeCard.id}`)">打开角色卡</el-button>
          </div>
        </section>

        <section class="side-section">
          <div class="section-head">
            <span>成员</span>
            <el-tag size="small">{{ members.length }}</el-tag>
          </div>
          <div class="member-list">
            <div v-for="member in members" :key="member.userId" class="member-row">
              <div class="member-avatar">{{ avatarText(member.userId) }}</div>
              <div class="member-main">
                <div class="member-name">{{ displayName(member.userId) }}</div>
                <div class="member-tags">
                  <el-tag size="small" :type="member.role === 'OWNER' ? 'warning' : 'info'">
                    {{ roleLabel(member.role) }}
                  </el-tag>
                  <el-tag v-if="member.muted" size="small" type="danger">禁言</el-tag>
                </div>
              </div>
              <div v-if="cardNameOf(member.cardId)" class="member-card">{{ cardNameOf(member.cardId) }}</div>
            </div>
          </div>
        </section>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const roomId = Number(route.params.roomId)

const loading = ref(false)
const sending = ref(false)
const room = ref(null)
const messages = ref([])
const members = ref([])
const myCards = ref([])
const activeCardId = ref(null)
const inputText = ref('')
const skillName = ref('')
const sendMode = ref('TEXT')
const messagesRef = ref(null)
const roomNickname = ref('')
const savingNickname = ref(false)
let pollTimer = null

const myId = computed(() => userStore.user?.id)
const activeCard = computed(() => myCards.value.find(card => card.id === activeCardId.value))
const memberNameMap = computed(() => {
  const map = {}
  members.value.forEach(member => {
    if (member.displayName) {
      map[member.userId] = member.displayName
    }
  })
  return map
})
const cardNameMap = computed(() => {
  const map = {}
  myCards.value.forEach(card => { map[card.id] = card.name })
  return map
})
const placeholder = computed(() => {
  if (sendMode.value === 'DICE') return '骰子表达式，例如：1d100、2d6+3'
  if (sendMode.value === 'CHECK') return '可选备注，例如：调查门口的脚印'
  return '输入消息，Ctrl+Enter 发送'
})

onMounted(async () => {
  loading.value = true
  try {
    await ensureUser()
    await refreshAll()
    pollTimer = setInterval(loadMessages, 3000)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

async function ensureUser() {
  if (userStore.user) return
  const { data } = await api.get('/users/me')
  userStore.user = data
}

async function refreshAll() {
  await Promise.all([loadRoom(), loadCards()])
  await loadMessages({ forceScroll: true })
}

async function loadRoom() {
  const [roomRes, membersRes] = await Promise.all([
    api.get(`/rooms/${roomId}`),
    api.get(`/rooms/${roomId}/members`)
  ])
  room.value = roomRes.data
  members.value = membersRes.data
  const me = members.value.find(member => member.userId === myId.value)
  activeCardId.value = me?.cardId ?? activeCardId.value
  roomNickname.value = me?.displayName ?? roomNickname.value
}

async function loadCards() {
  if (!myId.value) return
  const { data } = await api.get(`/player-cards/user/${myId.value}`)
  myCards.value = data
}

async function loadMessages(options = {}) {
  const list = messagesRef.value
  const shouldStickToBottom = options.forceScroll || isNearBottom(list)
  const previousScrollTop = list?.scrollTop ?? 0
  const previousScrollHeight = list?.scrollHeight ?? 0

  const { data } = await api.get(`/rooms/${roomId}/messages`)
  messages.value = data

  await nextTick()
  if (!messagesRef.value) return

  if (shouldStickToBottom) {
    scrollToBottom()
  } else {
    const heightDiff = messagesRef.value.scrollHeight - previousScrollHeight
    messagesRef.value.scrollTop = previousScrollTop + Math.max(0, heightDiff)
  }
}

async function send() {
  if (sending.value) return
  sending.value = true
  try {
    if (sendMode.value === 'TEXT') {
      await sendText()
    } else if (sendMode.value === 'DICE') {
      await sendDice()
    } else {
      await sendSkillCheck()
    }
    inputText.value = ''
    await loadMessages({ forceScroll: true })
  } finally {
    sending.value = false
  }
}

async function sendText() {
  const content = inputText.value.trim()
  if (!content) return
  await api.post(`/rooms/${roomId}/messages`, { content })
}

async function sendDice() {
  const expression = inputText.value.trim()
  if (!expression) return

  if (/^\d+#.+/.test(expression.replace(/\s+/g, ''))) {
    const { data } = await api.post('/dice/repeat', { expression })
    await api.post(`/rooms/${roomId}/messages/dice`, {
      content: formatRepeatRoll(data)
    })
    return
  }

  const { data } = await api.post('/dice/roll', { expression })
  await api.post(`/rooms/${roomId}/messages/dice`, {
    content: formatSingleRoll(data)
  })
}

async function sendSkillCheck() {
  if (!activeCardId.value) {
    ElMessage.warning('请先选择角色卡')
    return
  }
  const name = skillName.value.trim()
  if (!name) {
    ElMessage.warning('请输入技能名')
    return
  }
  const { data } = await api.post('/dice/skill-check', {
    cardId: activeCardId.value,
    skillName: name
  })
  const note = inputText.value.trim()
  const content = `${data.skillName} ${data.roll}/${data.skillValue}，${data.successLevel}${note ? `：${note}` : ''}`
  await api.post(`/rooms/${roomId}/messages/skill-check`, { content })
}

async function bindCard(cardId) {
  await api.put(`/rooms/${roomId}/members/card`, { cardId: cardId ?? null })
  const me = members.value.find(member => member.userId === myId.value)
  if (me) me.cardId = cardId ?? null
}

async function saveRoomNickname() {
  savingNickname.value = true
  try {
    await api.patch(`/rooms/${roomId}/members/me/nickname`, { nickname: roomNickname.value })
    const me = members.value.find(member => member.userId === myId.value)
    if (me) me.displayName = roomNickname.value.trim()
    ElMessage.success('房间昵称已保存')
  } finally {
    savingNickname.value = false
  }
}

async function leaveRoom() {
  await ElMessageBox.confirm('确定离开这个房间吗？', '离开房间', { type: 'warning' })
  await api.post(`/rooms/${roomId}/leave`)
  router.push('/')
}

function displayName(userId) {
  if (userId === 0) return '系统'
  if (memberNameMap.value[userId]) return memberNameMap.value[userId]
  if (userId === myId.value) return userStore.user?.nickname || '我'
  return `用户 ${userId}`
}

function avatarText(userId) {
  return displayName(userId).slice(0, 1).toUpperCase()
}

function roleLabel(role) {
  return role === 'OWNER' ? '房主' : role === 'KEEPER' ? '主持' : '玩家'
}

function cardNameOf(cardId) {
  return cardId ? cardNameMap.value[cardId] : ''
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatSingleRoll(result) {
  const details = result.details?.length ? ` (${result.details.join(', ')})` : ''
  return `${result.expression} = ${result.finalResult}${details}`
}

function formatRepeatRoll(result) {
  const lines = (result.results || []).map((item, index) => {
    return `${index + 1}. ${formatSingleRoll(item)}`
  })
  return `${result.repeat}#${result.expression}\n${lines.join('\n')}`
}

function isNearBottom(element) {
  if (!element) return true
  const distance = element.scrollHeight - element.scrollTop - element.clientHeight
  return distance < 80
}

function scrollToBottom() {
  if (!messagesRef.value) return
  messagesRef.value.scrollTop = messagesRef.value.scrollHeight
}
</script>

<style scoped>
.room-page {
  min-height: 100vh;
  background: #f4f6f8;
  color: #20252b;
}

.room-header {
  height: 64px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #d8dee6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.room-title-group,
.room-actions,
.message-meta,
.member-tags {
  display: flex;
  align-items: center;
}

.room-title-group {
  gap: 12px;
}

.back-button {
  padding-left: 0;
}

.room-title {
  font-size: 18px;
  font-weight: 700;
}

.room-subtitle {
  display: flex;
  gap: 12px;
  margin-top: 2px;
  font-size: 12px;
  color: #67717f;
}

.room-actions {
  gap: 8px;
}

.room-shell {
  height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  padding: 16px;
}

.chat-panel,
.side-section {
  background: #ffffff;
  border: 1px solid #d8dee6;
  border-radius: 8px;
}

.chat-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
}

.empty-state {
  color: #8a94a3;
  text-align: center;
  padding: 48px 0;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}

.message-row.mine {
  flex-direction: row-reverse;
}

.message-row.system {
  justify-content: center;
}

.avatar,
.member-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #40566f;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.message-bubble {
  max-width: min(720px, 72%);
  padding: 9px 11px;
  border-radius: 8px;
  background: #eef2f6;
  border: 1px solid #dce3eb;
}

.message-row.mine .message-bubble {
  background: #e5f3eb;
  border-color: #c8e5d3;
}

.message-bubble.dice,
.message-bubble.skill_check {
  background: #fff4df;
  border-color: #f1d29a;
}

.message-meta {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #687381;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.55;
}

.system-message {
  color: #687381;
  background: #eef2f6;
  border-radius: 999px;
  padding: 4px 12px;
  font-size: 12px;
}

.composer {
  border-top: 1px solid #d8dee6;
  padding: 12px;
}

.mode-tabs {
  margin-bottom: 10px;
}

.check-row,
.input-row {
  display: flex;
  gap: 8px;
}

.check-row {
  margin-bottom: 8px;
}

.card-select {
  width: 220px;
}

.send-button {
  width: 88px;
}

.side-panel {
  min-width: 0;
  overflow-y: auto;
}

.side-section {
  padding: 14px;
  margin-bottom: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 700;
}

.wide {
  width: 100%;
}

.room-nickname-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  margin-bottom: 10px;
}

.card-summary {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.card-name {
  min-width: 0;
  font-size: 13px;
  color: #40566f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-row {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 9px;
}

.member-main {
  min-width: 0;
}

.member-name {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-tags {
  gap: 6px;
  margin-top: 4px;
}

.member-card {
  grid-column: 2;
  font-size: 12px;
  color: #687381;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .room-shell {
    height: auto;
    grid-template-columns: 1fr;
  }

  .chat-panel {
    min-height: 70vh;
  }

  .message-bubble {
    max-width: 84%;
  }

  .check-row,
  .input-row {
    flex-direction: column;
  }

  .card-select,
  .send-button {
    width: 100%;
  }
}
</style>
