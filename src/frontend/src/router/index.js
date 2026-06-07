import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue'), meta: { auth: true } },
  { path: '/room/:roomId', name: 'Room', component: () => import('../views/RoomView.vue'), meta: { auth: true } },
  { path: '/card/:cardId', name: 'PlayerCard', component: () => import('../views/PlayerCardView.vue'), meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.token) {
    return { name: 'Login' }
  }
})

export default router
