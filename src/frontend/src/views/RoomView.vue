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
        <div class="message-toolbar">
          <el-radio-group v-model="messageViewMode" size="small" @change="changeMessageView">
            <el-radio-button label="visible">全部</el-radio-button>
            <el-radio-button label="public">公开</el-radio-button>
            <el-radio-button label="paged">分页</el-radio-button>
          </el-radio-group>
          <div v-if="messageViewMode === 'paged'" class="pager-tools">
            <el-button size="small" :disabled="messagePage <= 1" @click="changeMessagePage(messagePage - 1)">上一页</el-button>
            <span>{{ messagePage }} / {{ messagePageCount }}</span>
            <el-button size="small" :disabled="messagePage >= messagePageCount" @click="changeMessagePage(messagePage + 1)">下一页</el-button>
          </div>
        </div>
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
              <article
                class="message-bubble"
                :class="[message.type.toLowerCase(), { private: message.visibility === 'PRIVATE' }]"
              >
                <div class="message-meta">
                  <span class="message-author-line">
                    <span>{{ displayName(message.senderId) }}</span>
                    <el-tag v-if="message.visibility === 'PRIVATE'" size="small" type="warning" effect="plain">
                      {{ message.type === 'DICE' ? '暗骰' : '私密' }}
                    </el-tag>
                  </span>
                  <span class="message-tools">
                    <span>{{ formatTime(message.timestamp) }}</span>
                    <el-button
                      v-if="message.senderId === myId"
                      link
                      size="small"
                      type="danger"
                      @click="deleteMessage(message)"
                    >
                      删除
                    </el-button>
                  </span>
                </div>
                <div class="message-content">{{ message.content }}</div>
              </article>
            </template>
          </div>
        </div>

        <div class="composer">
          <el-radio-group v-model="sendMode" size="small" class="mode-tabs">
            <el-radio-button label="TEXT">消息</el-radio-button>
            <el-radio-button label="PRIVATE">私聊</el-radio-button>
            <el-radio-button label="DICE">掷骰</el-radio-button>
            <el-radio-button label="SAN">SC</el-radio-button>
            <el-radio-button label="CHECK">检定</el-radio-button>
          </el-radio-group>

          <div v-if="sendMode === 'PRIVATE'" class="check-row">
            <el-select v-model="privateReceiverId" placeholder="私聊对象" class="card-select">
              <el-option
                v-for="member in privateTargets"
                :key="member.userId"
                :label="displayName(member.userId)"
                :value="member.userId"
              />
            </el-select>
            <el-input v-model="inputText" placeholder="输入私聊内容" @keyup.enter="send" />
          </div>

          <div v-if="sendMode === 'DICE' || sendMode === 'SAN'" class="dice-options">
            <el-switch
              v-model="swtPrivateDice"
              active-text="暗骰"
              inactive-text="明骰"
              inline-prompt
              size="small"
            />
          </div>

          <div v-if="sendMode === 'SAN'" class="check-row">
            <el-input v-model="inputText" placeholder="SC 参数，例如：1/1d6 60" @keyup.enter="send" />
          </div>

          <div v-if="sendMode === 'CHECK'" class="check-row">
            <el-select v-model="activeCardId" placeholder="角色卡" class="card-select">
              <el-option v-for="card in myCards" :key="card.id" :label="card.name" :value="card.id" />
            </el-select>
            <el-input v-model="skillName" placeholder="技能名，例如：侦查" @keyup.enter="send" />
          </div>

          <div class="input-row">
            <el-input
              v-if="sendMode !== 'PRIVATE' && sendMode !== 'SAN'"
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
        <section v-if="isOwner" class="side-section">
          <div class="section-head">
            <span>房间设置</span>
            <el-tag size="small" type="warning">房主</el-tag>
          </div>
          <div class="room-setting-grid">
            <el-input v-model="editRoomName" size="small" placeholder="房间名" />
            <el-button size="small" :loading="savingRoomName" @click="saveRoomName">改名</el-button>
          </div>
          <el-input
            v-model="editRoomDescription"
            class="room-description"
            type="textarea"
            :rows="3"
            resize="vertical"
            placeholder="房间描述"
          />
          <el-button class="wide action-line" size="small" :loading="savingRoomDescription" @click="saveRoomDescription">
            保存描述
          </el-button>
          <div class="tag-list">
            <el-tag
              v-for="tag in room?.tags || []"
              :key="tag"
              closable
              size="small"
              @close="removeRoomTag(tag)"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="room-setting-grid">
            <el-input v-model="newRoomTag" size="small" placeholder="新标签" @keyup.enter="addRoomTag" />
            <el-button size="small" @click="addRoomTag">添加</el-button>
          </div>
          <div class="danger-actions">
            <el-button size="small" :disabled="room?.status !== 'OPEN'" @click="closeCurrentRoom">关闭</el-button>
            <el-button size="small" type="danger" plain @click="deleteCurrentRoom">删除</el-button>
          </div>
        </section>

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
                <div class="member-actions">
                  <el-button link size="small" @click="openPrivateHistory(member)">私聊记录</el-button>
                  <template v-if="isOwner && member.userId !== room?.ownerId">
                    <el-select
                      :model-value="member.role"
                      size="small"
                      class="role-select"
                      @change="role => changeMemberRole(member, role)"
                    >
                      <el-option label="玩家" value="PLAYER" />
                      <el-option label="Bot" value="BOT" />
                    </el-select>
                    <el-button
                      link
                      size="small"
                      :type="member.muted ? 'success' : 'danger'"
                      @click="member.muted ? unmuteMember(member) : muteMember(member)"
                    >
                      {{ member.muted ? '解禁' : '禁言' }}
                    </el-button>
                  </template>
                </div>
              </div>
              <div v-if="cardNameOf(member.cardId)" class="member-card">{{ cardNameOf(member.cardId) }}</div>
            </div>
          </div>
        </section>
      </aside>
    </main>

    <el-dialog v-model="showPrivateHistory" :title="privateHistoryTitle" width="620">
      <div class="history-list">
        <div v-if="privateHistory.length === 0" class="empty-state">暂无私聊记录</div>
        <div v-for="message in privateHistory" :key="message.id" class="history-row">
          <span>{{ displayName(message.senderId) }}</span>
          <span>{{ message.content }}</span>
          <small>{{ formatTime(message.timestamp) }}</small>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPrivateHistory = false">关闭</el-button>
      </template>
    </el-dialog>
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
const swtPrivateDice = ref(false)
const privateReceiverId = ref(null)
const messageViewMode = ref('visible')
const messagePage = ref(1)
const messageSize = ref(50)
const messageTotal = ref(0)
const messagesRef = ref(null)
const roomNickname = ref('')
const savingNickname = ref(false)
const editRoomName = ref('')
const editRoomDescription = ref('')
const newRoomTag = ref('')
const savingRoomName = ref(false)
const savingRoomDescription = ref(false)
const showPrivateHistory = ref(false)
const privateHistory = ref([])
const privateHistoryMember = ref(null)
let pollTimer = null

const myId = computed(() => userStore.user?.id)
const isOwner = computed(() => room.value?.ownerId === myId.value)
const privateTargets = computed(() => members.value.filter(member => member.userId !== myId.value))
const messagePageCount = computed(() => Math.max(1, Math.ceil(messageTotal.value / messageSize.value)))
const privateHistoryTitle = computed(() => {
  if (!privateHistoryMember.value) return '私聊记录'
  return `与 ${displayName(privateHistoryMember.value.userId)} 的私聊记录`
})
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
  if (sendMode.value === 'DICE') return '骰子表达式，例如：1d100、2d6+3、5#2d6kh1'
  if (sendMode.value === 'SAN') return 'SC 参数，例如：1/1d6 60'
  if (sendMode.value === 'CHECK') return '可选备注，例如：调查屋里的书架'
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
  editRoomName.value = room.value.name || ''
  editRoomDescription.value = room.value.description || ''
  members.value = membersRes.data
  const me = members.value.find(member => member.userId === myId.value)
  activeCardId.value = me?.cardId ?? activeCardId.value
  roomNickname.value = me?.displayName ?? roomNickname.value
  if (!privateReceiverId.value && privateTargets.value.length > 0) {
    privateReceiverId.value = privateTargets.value[0].userId
  }
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

  if (messageViewMode.value === 'public') {
    const { data } = await api.get(`/rooms/${roomId}/messages/public`)
    messages.value = data
    messageTotal.value = data.length
  } else if (messageViewMode.value === 'paged') {
    const { data } = await api.get(`/rooms/${roomId}/messages/paged`, {
      params: { page: messagePage.value, size: messageSize.value }
    })
    messages.value = data.items || []
    messageTotal.value = data.total || 0
  } else {
    const { data } = await api.get(`/rooms/${roomId}/messages`)
    messages.value = data
    messageTotal.value = data.length
  }

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
    } else if (sendMode.value === 'PRIVATE') {
      await sendPrivateText()
    } else if (sendMode.value === 'DICE') {
      await sendDice()
    } else if (sendMode.value === 'SAN') {
      await sendSanCheck()
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

async function sendPrivateText() {
  const content = inputText.value.trim()
  if (!content) return
  if (!privateReceiverId.value) {
    ElMessage.warning('请选择私聊对象')
    return
  }
  await api.post(`/rooms/${roomId}/messages/private`, {
    receiverId: privateReceiverId.value,
    content
  })
}

async function sendDice() {
  const expression = inputText.value.trim()
  if (!expression) return
  const messageUrl = swtPrivateDice.value
    ? `/rooms/${roomId}/messages/dice/private`
    : `/rooms/${roomId}/messages/dice`

  if (/^\d+#.+/.test(expression.replace(/\s+/g, ''))) {
    const { data } = await api.post('/dice/repeat', { expression })
    await api.post(messageUrl, {
      content: formatRepeatRoll(data)
    })
    return
  }

  const { data } = await api.post('/dice/roll', { expression })
  await api.post(messageUrl, {
    content: formatSingleRoll(data)
  })
}

async function sendSanCheck() {
  const args = inputText.value.trim()
  if (!args) return
  const { data } = await api.post('/dice/san-check', { args })
  const messageUrl = swtPrivateDice.value
    ? `/rooms/${roomId}/messages/dice/private`
    : `/rooms/${roomId}/messages/dice`
  await api.post(messageUrl, { content: formatSanCheck(data) })
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

async function saveRoomName() {
  savingRoomName.value = true
  try {
    await api.patch(`/rooms/${roomId}/name`, { name: editRoomName.value })
    room.value.name = editRoomName.value
    ElMessage.success('房间名已保存')
  } finally {
    savingRoomName.value = false
  }
}

async function saveRoomDescription() {
  savingRoomDescription.value = true
  try {
    await api.patch(`/rooms/${roomId}/description`, { description: editRoomDescription.value })
    room.value.description = editRoomDescription.value
    ElMessage.success('房间描述已保存')
  } finally {
    savingRoomDescription.value = false
  }
}

async function addRoomTag() {
  const tag = newRoomTag.value.trim()
  if (!tag) return
  await api.post(`/rooms/${roomId}/tags`, { tag })
  newRoomTag.value = ''
  await loadRoom()
}

async function removeRoomTag(tag) {
  await api.delete(`/rooms/${roomId}/tags`, { data: { tag } })
  await loadRoom()
}

async function closeCurrentRoom() {
  await ElMessageBox.confirm('确定关闭这个房间吗？', '关闭房间', { type: 'warning' })
  await api.post(`/rooms/${roomId}/close`)
  ElMessage.success('房间已关闭')
  await loadRoom()
}

async function deleteCurrentRoom() {
  await ElMessageBox.confirm('确定删除这个房间吗？', '删除房间', { type: 'warning' })
  await api.delete(`/rooms/${roomId}`)
  ElMessage.success('房间已删除')
  router.push('/')
}

async function changeMemberRole(member, role) {
  await api.patch(`/rooms/${roomId}/members/${member.userId}/role`, { role })
  member.role = role
}

async function muteMember(member) {
  await api.post(`/rooms/${roomId}/members/${member.userId}/mute`)
  member.muted = true
}

async function unmuteMember(member) {
  await api.post(`/rooms/${roomId}/members/${member.userId}/unmute`)
  member.muted = false
}

async function openPrivateHistory(member) {
  privateHistoryMember.value = member
  const { data } = await api.get(`/rooms/${roomId}/messages/private`, {
    params: { userBId: member.userId }
  })
  privateHistory.value = data
  showPrivateHistory.value = true
}

async function deleteMessage(message) {
  await ElMessageBox.confirm('确定删除这条消息吗？', '删除消息', { type: 'warning' })
  await api.delete(`/rooms/${roomId}/messages/${message.id}`)
  messages.value = messages.value.filter(item => item.id !== message.id)
}

async function changeMessageView() {
  messagePage.value = 1
  await loadMessages({ forceScroll: true })
}

async function changeMessagePage(page) {
  messagePage.value = page
  await loadMessages({ forceScroll: true })
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

function formatSanCheck(result) {
  const loss = result.lossResult
  const details = loss?.details?.length ? ` (${loss.details.join(', ')})` : ''
  return `SC ${result.roll}/${result.target}，${result.success ? '成功' : '失败'}，理智损失 ${loss.finalResult}${details}`
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

.message-toolbar {
  min-height: 44px;
  padding: 8px 12px;
  border-bottom: 1px solid #d8dee6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.pager-tools,
.message-tools {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.pager-tools {
  color: #687381;
  font-size: 12px;
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

.message-bubble.private {
  border-style: dashed;
}

.message-meta {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #687381;
}

.message-author-line {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 6px;
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

.dice-options {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
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

.room-setting-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  margin-bottom: 10px;
}

.room-description {
  margin-bottom: 8px;
}

.action-line {
  margin-bottom: 10px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
  margin-bottom: 10px;
}

.danger-actions {
  display: flex;
  gap: 8px;
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

.member-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.role-select {
  width: 92px;
}

.member-card {
  grid-column: 2;
  font-size: 12px;
  color: #687381;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-list {
  display: grid;
  gap: 8px;
  max-height: 420px;
  overflow-y: auto;
}

.history-row {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr) 64px;
  gap: 8px;
  align-items: start;
  font-size: 13px;
}

.history-row span:nth-child(2) {
  white-space: pre-wrap;
  word-break: break-word;
}

.history-row small {
  color: #8a94a3;
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

  .message-toolbar,
  .danger-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .card-select,
  .send-button {
    width: 100%;
  }

  .room-setting-grid,
  .history-row {
    grid-template-columns: 1fr;
  }
}
</style>
