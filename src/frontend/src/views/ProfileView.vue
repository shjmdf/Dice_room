<template>
  <div class="profile-page" v-loading="loading">
    <header class="topbar">
      <div>
        <div class="title">个人资料</div>
        <div class="subtitle">账号信息与公开展示信息</div>
      </div>
      <div class="actions">
        <el-button @click="router.push('/')">返回首页</el-button>
      </div>
    </header>

    <main class="profile-shell">
      <section class="profile-section">
        <div class="section-title">展示资料</div>
        <div class="form-grid">
          <label>
            昵称
            <el-input v-model="profile.nickname" />
          </label>
          <label>
            邮箱
            <el-input v-model="profile.email" />
          </label>
          <label class="wide">
            头像链接
            <el-input v-model="profile.avatarUrl" />
          </label>
          <label class="wide">
            简介
            <el-input v-model="profile.description" type="textarea" :rows="5" resize="vertical" />
          </label>
        </div>
        <div class="section-actions">
          <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
        </div>
      </section>

      <section class="profile-section">
        <div class="section-title">登录名</div>
        <div class="inline-form">
          <el-input v-model="loginName" placeholder="新的登录名" />
          <el-button type="primary" :loading="savingLoginName" @click="saveLoginName">修改登录名</el-button>
        </div>
      </section>

      <section class="profile-section">
        <div class="section-title">密码</div>
        <div class="form-grid password-grid">
          <label>
            旧密码
            <el-input v-model="password.oldPassword" type="password" show-password />
          </label>
          <label>
            新密码
            <el-input v-model="password.newPassword" type="password" show-password />
          </label>
        </div>
        <div class="section-actions">
          <el-button type="primary" :loading="savingPassword" @click="savePassword">修改密码</el-button>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const savingProfile = ref(false)
const savingLoginName = ref(false)
const savingPassword = ref(false)
const loginName = ref('')
const profile = reactive({
  nickname: '',
  avatarUrl: '',
  description: '',
  email: ''
})
const password = reactive({
  oldPassword: '',
  newPassword: ''
})

onMounted(loadUser)

async function loadUser() {
  loading.value = true
  try {
    const { data } = await api.get('/users/me')
    applyUser(data)
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    const { data } = await api.patch('/users/me/profile', { ...profile })
    applyUser(data)
    ElMessage.success('资料已保存')
  } finally {
    savingProfile.value = false
  }
}

async function saveLoginName() {
  savingLoginName.value = true
  try {
    const { data } = await api.patch('/users/me/login-name', { loginName: loginName.value })
    applyUser(data)
    ElMessage.success('登录名已修改')
  } finally {
    savingLoginName.value = false
  }
}

async function savePassword() {
  savingPassword.value = true
  try {
    await api.patch('/users/me/password', { ...password })
    password.oldPassword = ''
    password.newPassword = ''
    ElMessage.success('密码已修改')
  } finally {
    savingPassword.value = false
  }
}

function applyUser(user) {
  userStore.user = user
  loginName.value = user.loginName || ''
  profile.nickname = user.nickname || ''
  profile.avatarUrl = user.avatarUrl || ''
  profile.description = user.description || ''
  profile.email = user.email || ''
}
</script>

<style scoped>
.profile-page {
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

.profile-shell {
  max-width: 920px;
  margin: 0 auto;
  padding: 20px;
}

.profile-section {
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.password-grid {
  max-width: 620px;
}

.wide {
  grid-column: 1 / -1;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #687381;
}

.inline-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  max-width: 620px;
}

.section-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .form-grid,
  .inline-form {
    grid-template-columns: 1fr;
  }

  .section-actions {
    justify-content: stretch;
  }

  .section-actions .el-button,
  .inline-form .el-button {
    width: 100%;
  }
}
</style>
