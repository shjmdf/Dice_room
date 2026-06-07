import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  function setLogin(tokenValue, userValue) {
    token.value = tokenValue
    user.value = userValue
    localStorage.setItem('token', tokenValue)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, setLogin, logout }
})
