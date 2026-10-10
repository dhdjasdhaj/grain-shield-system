import { defineStore } from 'pinia'
import api from '../api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('grain_token') || '',
    user: JSON.parse(localStorage.getItem('grain_user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isDispatcher: (state) => ['ADMIN', 'DISPATCHER'].includes(state.user?.role)
  },
  actions: {
    async login(credentials) {
      const data = await api.post('/auth/login', credentials)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('grain_token', data.token)
      localStorage.setItem('grain_user', JSON.stringify(data.user))
    },
    async refreshProfile() {
      if (!this.token) return
      this.user = await api.get('/auth/me')
      localStorage.setItem('grain_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('grain_token')
      localStorage.removeItem('grain_user')
    }
  }
})
