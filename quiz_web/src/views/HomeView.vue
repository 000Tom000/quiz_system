<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/api'
import { showConfirmDialog } from 'vant'

const router = useRouter()
const auth = useAuthStore()

const favoriteSets = ref([])
const recentRecords = ref([])
const stats = ref({ totalSets: 0, totalDone: 0, correctRate: 0 })
const loading = ref(true)
const searchKeyword = ref('')
const searchResults = ref([])
const searchOpen = ref(false)

onMounted(async () => {
  if (!auth.isLoggedIn) {
    router.replace('/login')
    return
  }
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [visibleSets, records] = await Promise.all([
      api.get('/sets/visible'),
      api.get('/practice/records'),
    ])
    favoriteSets.value = visibleSets || []
    recentRecords.value = (records || []).slice(0, 5)

    stats.value.totalSets = favoriteSets.value.length
    stats.value.totalDone = (records || []).length

    if (records && records.length > 0) {
      const totalCorrect = records.reduce((sum, r) => sum + (r.correctCount || 0), 0)
      const totalQuestions = records.reduce((sum, r) => sum + (r.totalCount || 0), 0)
      stats.value.correctRate = totalQuestions > 0 ? Math.round((totalCorrect / totalQuestions) * 100) : 0
    }
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) { searchResults.value = []; return }
  try {
    searchResults.value = await api.get('/sets/search', { params: { keyword: kw } })
  } catch { /* handled */ }
}

function clearSearch() {
  searchKeyword.value = ''
  searchResults.value = []
  searchOpen.value = false
}

function startPractice(set) {
  router.push(`/practice/${set.id}`)
}

function startWrongPractice(setId) {
  router.push(setId ? `/wrong/${setId}` : '/wrong')
}

async function handleLogout() {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出登录吗？' })
    auth.logout()
    router.replace('/login')
  } catch { /* cancelled */ }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getMonth() + 1}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<template>
  <div class="home-page">
    <!-- ===== 顶部导航 ===== -->
    <header class="home-header">
      <div class="header-content">
        <div class="header-left">
          <div class="avatar-circle">
            {{ (auth.user?.nickname || auth.user?.username || '同')[0] }}
          </div>
          <div>
            <h2 class="greeting">{{ auth.user?.nickname || auth.user?.username || '同学' }}</h2>
            <span class="class-tag" v-if="auth.user?.className">{{ auth.user.className }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </header>

    <div class="home-body">
      <!-- ===== 统计卡片 ===== -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon stat-icon--sets">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.totalSets }}</span>
            <span class="stat-label">可用题库</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--done">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.totalDone }}</span>
            <span class="stat-label">刷题次数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--rate">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20V10"/><path d="M18 20V4"/><path d="M6 20v-4"/></svg>
          </div>
          <div class="stat-body">
            <span class="stat-num">{{ stats.correctRate }}%</span>
            <span class="stat-label">正确率</span>
          </div>
        </div>
      </div>

      <!-- ===== 搜索栏 ===== -->
      <div class="search-bar" :class="{ open: searchOpen }">
        <div class="search-input-wrap">
          <svg class="search-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input
            v-model="searchKeyword"
            class="search-input"
            type="text"
            placeholder="搜索试题集名称、科目或教师"
            @focus="searchOpen = true"
            @input="handleSearch"
          />
          <button v-if="searchOpen" class="search-cancel" @click="clearSearch">取消</button>
        </div>

        <!-- 搜索结果下拉 -->
        <div class="search-dropdown" v-if="searchOpen && searchResults.length > 0">
          <div
            class="search-item"
            v-for="item in searchResults"
            :key="item.id"
            @click="startPractice(item); clearSearch()"
          >
            <div class="search-item-main">
              <span class="search-item-title">{{ item.title }}</span>
              <span class="search-item-count">{{ item.questionCount || 0 }} 题</span>
            </div>
            <span class="search-item-sub" v-if="item.description">{{ item.description }}</span>
          </div>
        </div>
        <div class="search-dropdown" v-else-if="searchOpen && searchKeyword && searchResults.length === 0">
          <div class="search-empty">未找到相关试题集</div>
        </div>
      </div>

      <!-- ===== 快捷入口 ===== -->
      <div class="quick-actions">
        <button class="quick-btn" @click="startWrongPractice()">
          <div class="quick-icon wrong-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <span>总错题集</span>
        </button>
        <button class="quick-btn" @click="router.push('/records')">
          <div class="quick-icon record-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          </div>
          <span>全部记录</span>
        </button>
      </div>

      <!-- ===== 我的题库 ===== -->
      <section class="section">
        <div class="section-head">
          <h3 class="section-title">我的题库</h3>
          <span class="section-count" v-if="favoriteSets.length">{{ favoriteSets.length }} 个</span>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="skeleton-card" v-for="i in 3" :key="i">
            <div class="skeleton-line w-60"></div>
            <div class="skeleton-line w-90"></div>
          </div>
        </div>

        <div v-else-if="favoriteSets.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
          </div>
          <p class="empty-text">还没有可用的试题集</p>
          <p class="empty-hint">联系老师为你开放题库</p>
        </div>

        <div class="set-grid" v-else>
          <div
            class="set-card"
            v-for="item in favoriteSets"
            :key="item.id"
            @click="startPractice(item)"
          >
            <div class="set-card-top">
              <h4 class="set-name">{{ item.title }}</h4>
              <span class="set-count-badge">{{ item.questionCount || 0 }}题</span>
            </div>
            <p class="set-desc" v-if="item.description">{{ item.description }}</p>
            <div class="set-card-foot">
              <span class="set-teacher" v-if="item.teacherName">{{ item.teacherName }}</span>
              <button class="set-wrong-btn" @click.stop="startWrongPractice(item.id)">错题集</button>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== 最近记录 ===== -->
      <section class="section">
        <div class="section-head">
          <h3 class="section-title">最近记录</h3>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="skeleton-card" v-for="i in 2" :key="i">
            <div class="skeleton-line w-50"></div>
            <div class="skeleton-line w-70"></div>
          </div>
        </div>

        <div v-else-if="recentRecords.length === 0" class="empty-state small">
          <p class="empty-text">暂无刷题记录</p>
        </div>

        <div class="record-list" v-else>
          <div
            class="record-card"
            v-for="record in recentRecords"
            :key="record.id"
            @click="router.push(`/exam/record/${record.id}`)"
          >
            <div class="record-left">
              <div :class="['record-status-dot', record.finishedAt ? 'done' : 'going']"></div>
              <div>
                <p class="record-title">{{ record.setTitle || '试题集' }}</p>
                <p class="record-time">{{ formatTime(record.finishedAt || record.createdAt) }}</p>
              </div>
            </div>
            <div class="record-right">
              <span class="record-score">
                {{ record.correctCount || 0 }}/{{ record.totalCount || 0 }}
              </span>
              <span :class="['record-badge', record.finishedAt ? 'badge-done' : 'badge-going']">
                {{ record.finishedAt ? '已完成' : '进行中' }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <div class="bottom-spacer"></div>
    </div>
  </div>
</template>

<style scoped>
/* ===== 顶部 ===== */
.home-header {
  background: linear-gradient(135deg, #3366ff 0%, #5b8cff 100%);
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  max-width: var(--max-width-home);
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.greeting {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  line-height: 1.3;
}

.class-tag {
  font-size: 12px;
  opacity: 0.8;
  background: rgba(255, 255, 255, 0.15);
  padding: 2px 8px;
  border-radius: 10px;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* ===== 主体 ===== */
.home-body {
  padding: 0 16px;
  max-width: var(--max-width-home);
  margin: 0 auto;
}

/* ===== 统计卡 ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 16px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px 12px;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon--sets {
  background: #eef2ff;
  color: #3366ff;
}

.stat-icon--done {
  background: #e8f5e9;
  color: #07c160;
}

.stat-icon--rate {
  background: #fff3e0;
  color: #ff976a;
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

/* ===== 搜索 ===== */
.search-bar {
  margin-top: 16px;
  position: relative;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 0 14px;
  box-shadow: var(--shadow-sm);
  border: 1.5px solid transparent;
  transition: border-color 0.2s;
}

.search-bar.open .search-input-wrap {
  border-color: var(--color-primary);
}

.search-icon {
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--color-text);
  padding: 12px 10px;
  outline: none;
}

.search-input::placeholder {
  color: var(--color-text-muted);
}

.search-cancel {
  flex-shrink: 0;
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 14px;
  cursor: pointer;
  padding: 4px 0 4px 8px;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  margin-top: 4px;
  max-height: 240px;
  overflow-y: auto;
  z-index: 50;
}

.search-item {
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border);
  transition: background 0.15s;
}

.search-item:last-child {
  border-bottom: none;
}

.search-item:hover {
  background: #f8f9fb;
}

.search-item-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.search-item-count {
  font-size: 12px;
  color: var(--color-text-muted);
}

.search-item-sub {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 4px;
  display: block;
}

.search-empty {
  padding: 24px;
  text-align: center;
  font-size: 14px;
  color: var(--color-text-muted);
}

/* ===== 快捷入口 ===== */
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 16px;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: none;
  border-radius: var(--radius-md);
  padding: 14px 16px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  transition: box-shadow 0.2s, transform 0.15s;
}

.quick-btn:hover {
  box-shadow: var(--shadow-md);
}

.quick-btn:active {
  transform: scale(0.98);
}

.quick-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wrong-icon {
  background: #ffebee;
  color: #ee0a24;
}

.record-icon {
  background: #e3f2fd;
  color: #2196f3;
}

/* ===== 区块 ===== */
.section {
  margin-top: 24px;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text);
}

.section-count {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* ===== 题库网格（移动端单列，PC 双列） ===== */
.set-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.set-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.15s;
}

.set-card:hover {
  box-shadow: var(--shadow-md);
}

.set-card:active {
  transform: scale(0.99);
}

.set-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.set-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.set-count-badge {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary);
  background: #eef2ff;
  padding: 3px 8px;
  border-radius: 6px;
}

.set-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 8px 0 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.set-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.set-teacher {
  font-size: 12px;
  color: var(--color-text-muted);
}

.set-wrong-btn {
  background: none;
  border: 1px solid var(--color-warning);
  color: var(--color-warning);
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.set-wrong-btn:hover {
  background: #fff7f0;
}

/* ===== 刷题记录 ===== */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 14px 16px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.record-card:hover {
  box-shadow: var(--shadow-md);
}

.record-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.record-status-dot.done {
  background: var(--color-success);
}

.record-status-dot.going {
  background: var(--color-warning);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.record-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin: 0;
}

.record-time {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 2px 0 0;
}

.record-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.record-score {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.record-badge {
  font-size: 11px;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 4px;
}

.badge-done {
  background: #e8f5e9;
  color: #07c160;
}

.badge-going {
  background: #fff3e0;
  color: #ff976a;
}

/* ===== 加载骨架 ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.skeleton-line {
  height: 14px;
  background: #e5e7eb;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-line:last-child {
  margin-bottom: 0;
}

.w-60 { width: 60%; }
.w-90 { width: 90%; }
.w-50 { width: 50%; }
.w-70 { width: 70%; }

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 40px 16px;
}

.empty-state.small {
  padding: 28px 16px;
}

.empty-icon {
  color: #d1d5db;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0;
}

.empty-hint {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 6px 0 0;
}

/* ===== 底部间距 ===== */
.bottom-spacer {
  height: 24px;
}

/* ===== PC 适配 ===== */
@media (min-width: 768px) {
  .home-body {
    padding: 0 24px;
  }

  .set-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-row {
    gap: 16px;
  }

  .stat-card {
    flex-direction: row;
    padding: 20px;
    gap: 14px;
  }

  .stat-body {
    align-items: flex-start;
  }

  .quick-actions {
    grid-template-columns: repeat(4, 1fr);
  }

  .header-content {
    padding: 16px 24px;
  }
}

@media (min-width: 1024px) {
  .home-page {
    border-radius: var(--radius-lg);
    overflow: hidden;
    margin: 16px auto;
    min-height: calc(100vh - 32px);
  }
}
</style>
