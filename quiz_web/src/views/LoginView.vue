<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { showToast, showSuccessToast } from 'vant'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const loginMode = ref('password')
const submitting = ref(false)

// 密码登录
const pwdForm = reactive({ username: '', password: '' })

// 验证码登录
const codeForm = reactive({ email: '', code: '' })
const codeSending = ref(false)
const codeCountdown = ref(0)
let codeTimer = null

onUnmounted(() => clearInterval(codeTimer))

async function handlePwdLogin() {
  if (!pwdForm.username.trim()) return showToast('请输入用户名')
  if (!pwdForm.password) return showToast('请输入密码')
  submitting.value = true
  try {
    await auth.loginByPassword(pwdForm.username.trim(), pwdForm.password)
    showSuccessToast('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    submitting.value = false
  }
}

async function handleCodeLogin() {
  if (!codeForm.email.trim()) return showToast('请输入邮箱')
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(codeForm.email)) return showToast('邮箱格式不正确')
  if (!codeForm.code.trim()) return showToast('请输入验证码')
  submitting.value = true
  try {
    await auth.loginByCode(codeForm.email.trim(), codeForm.code.trim())
    showSuccessToast('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    submitting.value = false
  }
}

async function handleSendCode() {
  if (!codeForm.email.trim()) return showToast('请输入邮箱')
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(codeForm.email)) return showToast('邮箱格式不正确')
  if (codeCountdown.value > 0) return
  codeSending.value = true
  try {
    await auth.sendCode(codeForm.email.trim(), 'login')
    showSuccessToast('验证码已发送')
    codeCountdown.value = 60
    codeTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) { clearInterval(codeTimer); codeTimer = null }
    }, 1000)
  } finally {
    codeSending.value = false
  }
}
</script>

<template>
  <div class="auth-page login-page">
    <!-- PC 端卡片容器 -->
    <div class="auth-card">
      <!-- Logo 区 -->
      <div class="auth-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
            <rect x="4" y="6" width="40" height="36" rx="6" fill="#3366ff" />
            <path d="M14 22l6 6 12-12" stroke="#fff" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <h1 class="brand-title">刷题系统</h1>
        <p class="brand-desc">高效刷题，轻松提分</p>
      </div>

      <!-- Tab 切换 -->
      <div class="login-tabs">
        <button
          :class="['tab-btn', { active: loginMode === 'password' }]"
          @click="loginMode = 'password'"
        >
          密码登录
        </button>
        <button
          :class="['tab-btn', { active: loginMode === 'code' }]"
          @click="loginMode = 'code'"
        >
          验证码登录
        </button>
      </div>

      <!-- 密码登录表单 -->
      <form v-if="loginMode === 'password'" class="auth-form" @submit.prevent="handlePwdLogin">
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M6 20v-2a4 4 0 014-4h4a4 4 0 014 4v2"/></svg>
          </span>
          <input
            v-model="pwdForm.username"
            class="input-field"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
          />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
          </span>
          <input
            v-model="pwdForm.password"
            class="input-field"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>
        <button class="submit-btn" type="submit" :disabled="submitting">
          <span v-if="submitting" class="spinner"></span>
          {{ submitting ? '登录中...' : '登 录' }}
        </button>
      </form>

      <!-- 验证码登录表单 -->
      <form v-if="loginMode === 'code'" class="auth-form" @submit.prevent="handleCodeLogin">
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 7l-10 7L2 7"/></svg>
          </span>
          <input
            v-model="codeForm.email"
            class="input-field"
            type="email"
            placeholder="请输入邮箱"
            autocomplete="email"
          />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
          </span>
          <input
            v-model="codeForm.code"
            class="input-field"
            type="text"
            placeholder="请输入验证码"
            maxlength="6"
            autocomplete="one-time-code"
          />
          <button
            type="button"
            class="code-btn"
            :disabled="codeCountdown > 0 || codeSending"
            @click="handleSendCode"
          >
            {{ codeCountdown > 0 ? `${codeCountdown}s` : codeSending ? '发送中' : '获取验证码' }}
          </button>
        </div>
        <button class="submit-btn" type="submit" :disabled="submitting">
          <span v-if="submitting" class="spinner"></span>
          {{ submitting ? '登录中...' : '登 录' }}
        </button>
      </form>

      <!-- 底部 -->
      <div class="auth-footer">
        <router-link to="/register" class="footer-link">没有账号？立即注册</router-link>
        <p class="footer-tip">如需教师账号，请联系管理员</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

/* ===== PC 卡片 ===== */
.auth-card {
  width: 100%;
  max-width: 400px;
}

/* ===== Brand ===== */
.auth-brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  display: inline-flex;
  margin-bottom: 12px;
}

.brand-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 4px;
}

.brand-desc {
  font-size: 14px;
  color: var(--color-text-muted);
}

/* ===== Tabs ===== */
.login-tabs {
  display: flex;
  background: #e8ecf1;
  border-radius: var(--radius-sm);
  padding: 3px;
  margin-bottom: 24px;
}

.tab-btn {
  flex: 1;
  padding: 10px 0;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.25s;
}

.tab-btn.active {
  background: #fff;
  color: var(--color-text);
  box-shadow: var(--shadow-sm);
}

/* ===== 表单 ===== */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f6f8;
  border: 1.5px solid transparent;
  border-radius: var(--radius-md);
  padding: 0 14px;
  transition: border-color 0.2s, background 0.2s;
}

.input-group:focus-within {
  border-color: var(--color-primary);
  background: #fff;
}

.input-icon {
  display: flex;
  align-items: center;
  color: var(--color-text-muted);
  margin-right: 10px;
  flex-shrink: 0;
}

.input-field {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  color: var(--color-text);
  padding: 14px 0;
  outline: none;
  min-width: 0;
}

.input-field::placeholder {
  color: var(--color-text-muted);
}

.code-btn {
  flex-shrink: 0;
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 6px 0 6px 10px;
  white-space: nowrap;
}

.code-btn:disabled {
  color: var(--color-text-muted);
  cursor: not-allowed;
}

/* ===== 提交按钮 ===== */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
  margin-top: 4px;
}

.submit-btn:hover {
  background: var(--color-primary-dark);
}

.submit-btn:active {
  transform: scale(0.995);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 小 spinner */
.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 底部链接 ===== */
.auth-footer {
  text-align: center;
  margin-top: 24px;
}

.footer-link {
  font-size: 14px;
  color: var(--color-primary);
}

.footer-tip {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 12px;
}

/* ===== 移动端微调 ===== */
@media (max-width: 767px) {
  .login-page {
    padding: 24px 20px;
    align-items: flex-start;
  }

  .auth-card {
    margin-top: 20px;
  }
}
</style>
