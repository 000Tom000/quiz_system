import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
  // ===== 状态 =====
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  // ===== 计算属性 =====
  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || 'student')

  // ===== 密码登录 =====
  async function loginByPassword(username, password) {
    const data = await api.post('/auth/login', {
      username,
      password,
      type: 'password',
    })
    _setLogin(data)
    return data
  }

  // ===== 邮箱验证码登录 =====
  async function loginByCode(email, code) {
    const data = await api.post('/auth/login', {
      email,
      code,
      type: 'code',
    })
    _setLogin(data)
    return data
  }

  // ===== 注册 =====
  async function register({ username, password, email, code, studentNo, realName }) {
    const data = await api.post('/auth/register', {
      username,
      password,
      email,
      code,
      studentNo,
      realName,
    })
    _setLogin(data)
    return data
  }

  // ===== 发送邮箱验证码 =====
  async function sendCode(email, purpose = 'login') {
    await api.post('/auth/send-code', { email, purpose })
  }

  // ===== 重置密码 =====
  async function resetPassword(email, code, newPassword) {
    await api.post('/auth/reset-password', { email, code, newPassword })
  }

  // ===== 获取当前用户信息 =====
  async function fetchProfile() {
    const data = await api.get('/auth/profile')
    user.value = data
    return data
  }

  // ===== 修改密码 =====
  async function updatePassword(oldPassword, newPassword) {
    await api.post('/auth/update-password', { oldPassword, newPassword })
  }

  // ===== 退出 =====
  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
  }

  // ===== 内部：保存登录态 =====
  function _setLogin(data) {
    user.value = data
    token.value = data.token
    localStorage.setItem('token', data.token)
  }

  return {
    user,
    token,
    isLoggedIn,
    role,
    loginByPassword,
    loginByCode,
    register,
    sendCode,
    resetPassword,
    fetchProfile,
    updatePassword,
    logout,
  }
})
