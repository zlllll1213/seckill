import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')

  const isLoggedIn = computed(() => !!username.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  function setUser(u, r) {
    username.value = u
    role.value = r
    localStorage.setItem('username', u)
    localStorage.setItem('role', r)
  }

  function logout() {
    username.value = ''
    role.value = ''
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  return { username, role, isLoggedIn, isAdmin, setUser, logout }
})
