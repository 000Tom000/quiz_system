<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { showToast, showSuccessToast } from 'vant'

const router = useRouter()
const auth = useAuthStore()

const step = ref(0) // 0=信息, 1=验证, 2=密码
const submitting = ref(false)

const form = reactive({
  studentNo: '',
  realName: '',
  email: '',
  code: '',
  username: '',
  password: '',
  confirmPassword: '',
})

const codeSending = ref(false)
const codeCountdown = ref(0)
let codeTimer = null

onUnmounted(() => clearInterval(codeTimer))

function startCountdown() {
  codeCountdown.value = 60
  clearInterval(codeTimer)
  codeTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) { clearInterval(codeTimer); codeTimer = null }
  }, 1000)
}

async function handleNext() {
  if (!form.studentNo.trim()) return showToast('请输入学号')
  if (!form.realName.trim()) return showToast('请输入姓名')
  if (!form.email.trim()) return showToast('请输入邮箱')
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return showToast('邮箱格式不正确')

  codeSending.value = true
  try {
    await auth.sendCode(form.email.trim(), 'register')
    showSuccessToast('验证码已发送')
    step.value = 1
    startCountdown()
  } finally {
    codeSending.value = false
  }
}

function handleVerify() {
  if (!form.code.trim()) return showToast('请输入验证码')
  step.value = 2
}

async function handleRegister() {
  if (!form.username.trim()) return showToast('请输入用户名')
  if (form.username.trim().length < 3) return showToast('用户名至少3位')
  if (!form.password) return showToast('请输入密码')
  if (form.password.length < 6) return showToast('密码至少6位')
  if (form.password !== form.confirmPassword) return showToast('两次密码不一致')

  submitting.value = true
  try {
    await auth.register({
      username: form.username.trim(),
      password: form.password,
      email: form.email.trim(),
      code: form.code.trim(),
      studentNo: form.studentNo.trim(),
      realName: form.realName.trim(),
    })
    showSuccessToast('注册成功')
    router.replace('/')
  } finally {
    submitting.value = false
  }
}

async function resendCode() {
  if (codeCountdown.value > 0) return
  codeSending.value = true
  try {
    await auth.sendCode(form.email.trim(), 'register')
    showSuccessToast('验证码已重新发送')
    startCountdown()
  } finally {
    codeSending.value = false
  }
}
</script>

<template>
  <div class="auth-page register-page">
    <div class="auth-card">
      <!-- 头部 -->
      <div class="reg-header">
        <button class="back-btn" @click="step > 0 ? step-- : router.back()">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <h2 class="reg-title">注册账号</h2>
      </div>

      <!-- 步骤条 -->
      <div class="step-bar">
        <div :class="['step-dot', { done: step >= 0, active: step === 0 }]">
          <span class="dot-inner">{{ step > 0 ? '✓' : '1' }}</span>
        </div>
        <div :class="['step-line', { done: step >= 1 }]"></div>
        <div :class="['step-dot', { done: step >= 1, active: step === 1 }]">
          <span class="dot-inner">{{ step > 1 ? '✓' : '2' }}</span>
        </div>
        <div :class="['step-line', { done: step >= 2 }]"></div>
        <div :class="['step-dot', { done: step >= 2, active: step === 2 }]">
          <span class="dot-inner">3</span>
        </div>
      </div>
      <div class="step-labels">
        <span :class="['step-label', { on: step === 0 }]">填写信息</span>
        <span :class="['step-label', { on: step === 1 }]">验证邮箱</span>
        <span :class="['step-label', { on: step === 2 }]">设置密码</span>
      </div>

      <!-- Step 0: 学校信息 -->
      <form v-if="step === 0" class="auth-form" @submit.prevent="handleNext">
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="9" x2="15" y2="9"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="12" y2="17"/></svg>
          </span>
          <input v-model="form.studentNo" class="input-field" type="text" placeholder="请输入学号" />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M6 20v-2a4 4 0 014-4h4a4 4 0 014 4v2"/></svg>
          </span>
          <input v-model="form.realName" class="input-field" type="text" placeholder="请输入真实姓名" />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 7l-10 7L2 7"/></svg>
          </span>
          <input v-model="form.email" class="input-field" type="email" placeholder="请输入邮箱" />
        </div>

        <div class="notice-banner">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
          <span>系统将根据学号和姓名核验身份，匹配成功后发送验证码</span>
        </div>

        <button class="submit-btn" type="submit" :disabled="codeSending">
          <span v-if="codeSending" class="spinner"></span>
          {{ codeSending ? '发送验证码...' : '下一步' }}
        </button>
      </form>

      <!-- Step 1: 验证邮箱 -->
      <form v-if="step === 1" class="auth-form" @submit.prevent="handleVerify">
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 7l-10 7L2 7"/></svg>
          </span>
          <input class="input-field" type="email" :value="form.email" disabled />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
          </span>
          <input v-model="form.code" class="input-field" type="text" placeholder="请输入6位验证码" maxlength="6" />
          <button type="button" class="code-btn" :disabled="codeCountdown > 0 || codeSending" @click="resendCode">
            {{ codeCountdown > 0 ? `${codeCountdown}s` : '重新发送' }}
          </button>
        </div>
        <button class="submit-btn" type="submit">验证邮箱</button>
      </form>

      <!-- Step 2: 设置密码 -->
      <form v-if="step === 2" class="auth-form" @submit.prevent="handleRegister">
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M6 20v-2a4 4 0 014-4h4a4 4 0 014 4v2"/></svg>
          </span>
          <input v-model="form.username" class="input-field" type="text" placeholder="设置用户名（至少3位）" />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
          </span>
          <input v-model="form.password" class="input-field" type="password" placeholder="设置密码（至少6位）" />
        </div>
        <div class="input-group">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
          </span>
          <input v-model="form.confirmPassword" class="input-field" type="password" placeholder="确认密码" />
        </div>
        <button class="submit-btn" type="submit" :disabled="submitting">
          <span v-if="submitting" class="spinner"></span>
          {{ submitting ? '注册中...' : '完成注册' }}
        </button>
      </form>

      <!-- 底部 -->
      <div class="auth-footer">
        <router-link to="/login" class="footer-link">已有账号？立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

/* ===== 头部 ===== */
.reg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 28px;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: var(--radius-sm);
  background: #f0f1f3;
  color: var(--color-text);
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #e2e4e8;
}

.reg-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

/* ===== 步骤条 ===== */
.step-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e7eb;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s;
  flex-shrink: 0;
}

.step-dot.active {
  background: var(--color-primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(51, 102, 255, 0.35);
}

.step-dot.done {
  background: var(--color-success);
  color: #fff;
}

.step-line {
  width: 48px;
  height: 2px;
  background: #e5e7eb;
  transition: background 0.3s;
  margin: 0 4px;
}

.step-line.done {
  background: var(--color-success);
}

.step-labels {
  display: flex;
  justify-content: space-between;
  margin-bottom: 28px;
  padding: 0 8px;
}

.step-label {
  font-size: 12px;
  color: var(--color-text-muted);
  text-align: center;
  transition: color 0.3s;
}

.step-label.on {
  color: var(--color-primary);
  font-weight: 600;
}

/* ===== 表单（复用 LoginView 的样式） ===== */
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

.input-field:disabled {
  color: var(--color-text-secondary);
  opacity: 0.7;
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

/* ===== 提示条 ===== */
.notice-banner {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 14px;
  background: #eef2ff;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: #4c51bf;
  line-height: 1.5;
}

.notice-banner svg {
  margin-top: 1px;
  flex-shrink: 0;
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

/* ===== 底部 ===== */
.auth-footer {
  text-align: center;
  margin-top: 24px;
}

.footer-link {
  font-size: 14px;
  color: var(--color-primary);
}

/* ===== 移动端 ===== */
@media (max-width: 767px) {
  .register-page {
    padding: 20px;
    align-items: flex-start;
  }

  .auth-card {
    margin-top: 8px;
  }
}
</style>
