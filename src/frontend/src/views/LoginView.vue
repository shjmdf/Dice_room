<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">DiceRoom</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form @submit.prevent="handleLogin">
            <el-form-item>
              <el-input v-model="loginForm.loginName" placeholder="登录名" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
            </el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form @submit.prevent="handleRegister">
            <el-form-item>
              <el-input v-model="registerForm.loginName" placeholder="登录名" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" type="password" placeholder="密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.nickname" placeholder="昵称" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.inviteCode" placeholder="邀请码" />
            </el-form-item>
            <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <el-divider />
      <el-collapse>
        <el-collapse-item title="首次使用？初始化管理员" name="admin">
          <el-form @submit.prevent="handleInitAdmin">
            <el-form-item>
              <el-input v-model="adminForm.loginName" placeholder="管理员登录名" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="adminForm.password" type="password" placeholder="管理员密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-input v-model="adminForm.nickname" placeholder="管理员昵称" />
            </el-form-item>
            <el-button type="warning" @click="handleInitAdmin" :loading="loading" style="width:100%">初始化管理员</el-button>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ loginName: '', password: '' })
const registerForm = reactive({ loginName: '', password: '', nickname: '', inviteCode: '' })
const adminForm = reactive({ loginName: '', password: '', nickname: '' })

async function handleLogin() {
  loading.value = true
  try {
    const { data } = await api.post('/users/login', {
      loginName: loginForm.loginName.trim(),
      password: loginForm.password
    })
    userStore.setLogin(data.token, data.user)
    router.push('/')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    await api.post('/users/register', {
      loginName: registerForm.loginName.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim(),
      inviteCode: registerForm.inviteCode.trim()
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
  } finally {
    loading.value = false
  }
}

async function handleInitAdmin() {
  loading.value = true
  try {
    await api.post('/users/admin/initialize', {
      loginName: adminForm.loginName.trim(),
      password: adminForm.password,
      nickname: adminForm.nickname.trim()
    })
    ElMessage.success('管理员初始化成功，请登录')
    activeTab.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
.login-card { width: 400px; }
.title { text-align: center; margin: 0 0 20px; color: #303133; }
</style>
