import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'


export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const userRole = ref(localStorage.getItem('userRole') || null)
  const username = ref(localStorage.getItem('username') || null)
  const userId = ref(localStorage.getItem('userId') || null)

  const isLoggedIn = computed(() => !!token.value)
  const getToken = computed(() => token.value)
  const getUserRole = computed(() => userRole.value)
  const getUsername = computed(() => username.value)
  const getUserId = computed(() => userId.value)
  const isSuperAdmin = computed(() => userRole.value === 'SUPER_ADMIN')

  async function login(loginUsername, password, role) {
    const { data } = await request({
      url: '/auth/login',
      method: 'post',
      data: {
        username: loginUsername,
        password: password,
        role: role
      }
    })

    token.value = data.token
    userRole.value = data.role
    username.value = data.username
    userId.value = data.userId

    // 将 token 和用户信息存入 localStorage
    localStorage.setItem('token', token.value)
    localStorage.setItem('userRole', userRole.value)
    localStorage.setItem('username', username.value)
    localStorage.setItem('userId', userId.value)
  }

  function logout() {
    token.value = null
    userRole.value = null
    username.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('username')
    window.location.href = '/login'
  }

  return {
    token,
    userRole,
    username,
    isLoggedIn,
    getToken,
    getUserRole,
    getUsername,
    getUserId,
    isSuperAdmin,
    login,
    logout,
  }
}) 