<template>
  <div class="dashboard">
    <!-- <div class="welcome-section">
      <div class="welcome-text" style="display: flex; flex-direction: column; align-items: center;">
        <h2>欢迎回来，{{ username }}</h2>
        <p class="role-text">{{ userRole }}</p>
      </div>
    </div> -->

  <div class="welcome-section dynamic-welcome">
    <div class="welcome-content" style="display: flex; flex-direction: column; align-items: center;">
      <h2>欢迎回来，{{ username }}</h2>
      <div class="role-tag">{{ userRole }}</div>
    </div>
    <div class="dynamic-bg"></div>
  </div>


    <div class="quick-access">
      <h3 class="section-title">快捷入口</h3>
      <div class="card-grid">
        <!-- 管理员菜单 -->
        <template v-if="!isStudent">
          <el-card class="access-card announcement-card" @click="router.push('/announcement/list')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Bell /></el-icon>
                <span>公告管理</span>
              </div>
            </template>
            <div class="card-content">
              <p>公告的增删改查</p>
            </div>
          </el-card>
          
          <el-card class="access-card student-card" @click="router.push('/student/list')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><User /></el-icon>
                <span>考生管理</span>
              </div>
            </template>
            <div class="card-content">
              <p>考生信息的增删改查</p>
            </div>
          </el-card>

          <el-card v-if="isSuperAdmin" class="access-card admin-card" @click="router.push('/admin/list')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><UserFilled /></el-icon>
                <span>老师管理</span>
              </div>
            </template>
            <div class="card-content">
              <p>老师账号管理</p>
            </div>
          </el-card>

          <el-card class="access-card exam-card" @click="router.push('/exam/list')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Document /></el-icon>
                <span>考试管理</span>
              </div>
            </template>
            <div class="card-content">
              <p>考试的创建和安排管理</p>
            </div>
          </el-card>

          <el-card class="access-card paper-card" @click="router.push('/exam/paper')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Document /></el-icon>
                <span>试卷管理</span>
              </div>
            </template>
            <div class="card-content">
              <p>试卷的创建和题目管理</p>
            </div>
          </el-card>

          <el-card class="access-card room-card" @click="router.push('/exam-room/list')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Location /></el-icon>
                <span>考场管理</span>
              </div>
            </template>
            <div class="card-content">
              <p>考场信息和考试安排管理</p>
            </div>
          </el-card>
        </template>

        <!-- 学生菜单 -->
        <template v-else>
          <el-card class="access-card profile-card" @click="router.push('/profile')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><User /></el-icon>
                <span>个人中心</span>
              </div>
            </template>
            <div class="card-content">
              <p>查看和修改个人信息</p>
            </div>
          </el-card>

          <el-card class="access-card register-card" @click="router.push('/exam/register')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Document /></el-icon>
                <span>考试报名</span>
              </div>
            </template>
            <div class="card-content">
              <p>浏览和报名考试</p>
            </div>
          </el-card>

          <el-card class="access-card myexam-card" @click="router.push('/exam/my')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Document /></el-icon>
                <span>我的考试</span>
              </div>
            </template>
            <div class="card-content">
              <p>查看和参加考试</p>
            </div>
          </el-card>

          <el-card class="access-card history-card" @click="router.push('/exam/history')" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="icon"><Document /></el-icon>
                <span>考试记录</span>
              </div>
            </template>
            <div class="card-content">
              <p>查看考试报名记录</p>
            </div>
          </el-card>
        </template>

        <el-card class="access-card discussion-card" @click="router.push('/discussion/list')" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon class="icon"><ChatDotSquare /></el-icon>
              <span>讨论区</span>
            </div>
          </template>
          <div class="card-content">
            <p>参与讨论</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { User, UserFilled, Document, Location, Bell, ChatSquare, ChatDotSquare } from '@element-plus/icons-vue'
import request from '../utils/request'
const authStore = useAuthStore()
const router = useRouter()

const username = computed(() => authStore.getUsername)
const userRole = computed(() => {
  switch (authStore.getUserRole) {
    case 'SUPER_ADMIN':
      return '超级管理员'
    case 'ADMIN':
      return '教师'
    case 'STUDENT':
      return '学生'
    default:
      return ''
  }
})
const isSuperAdmin = computed(() => authStore.getUserRole === 'SUPER_ADMIN')
const isStudent = computed(() => authStore.getUserRole === 'STUDENT')

async function checkToken() {
  await request({
    url: '/auth/check-token',
    method: 'get'
  })
}

onMounted(() => {
  checkToken()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 60px);
}

// .welcome-section {
//   background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
//   padding: 24px;
//   border-radius: 8px;
//   color: white;
//   margin-bottom: 24px;
//   box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
//   position: relative;
//   overflow: hidden;
  
//   &::after {
//     content: '';
//     position: absolute;
//     top: -50%;
//     right: -50%;
//     width: 100%;
//     height: 200%;
//     background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
//     transform: rotate(30deg);
//   }
// }

.dynamic-welcome {
  position: relative;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 32px;
  overflow: hidden;
  color: #1a1a1a;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.dynamic-welcome .welcome-content {
  position: relative;
  z-index: 3;
}

.dynamic-welcome h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.dynamic-welcome .role-tag {
  display: inline-block;
  margin-top: 12px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(5px);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: #1a1a1a;
}

.dynamic-welcome .dynamic-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 调整后的渐变色 - 比之前稍深但仍保持柔和 */
  background: linear-gradient(-45deg, 
    rgba(210, 220, 255, 0.9), 
    rgba(220, 210, 255, 0.9), 
    rgba(210, 255, 240, 0.9), 
    rgba(210, 230, 255, 0.9));
  background-size: 400% 400%;
  z-index: 1;
  animation: gradientBG 12s ease infinite;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}




.welcome-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
  z-index: 1;
  
  h2 {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
}

.role-text {
  margin: 8px 0 0;
  font-size: 16px;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.15);
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.section-title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  position: relative;
  padding-bottom: 8px;
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #4f46e5 0%, #7c3aed 100%);
    border-radius: 3px;
  }
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.access-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
  border-radius: 12px;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
  }
  
  .icon {
    font-size: 22px;
  }
  
  .card-content {
    color: #64748b;
    p {
      margin: 8px 0 0;
      font-size: 14px;
      line-height: 1.5;
    }
  }
}

// 为不同类型的卡片添加不同的颜色主题
.announcement-card {
  --card-color: #3b82f6;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.student-card {
  --card-color: #10b981;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.admin-card {
  --card-color: #8b5cf6;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.exam-card {
  --card-color: #ef4444;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.paper-card {
  --card-color: #f59e0b;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.room-card {
  --card-color: #6366f1;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.profile-card {
  --card-color: #3b82f6;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.register-card {
  --card-color: #10b981;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.myexam-card {
  --card-color: #f59e0b;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.history-card {
  --card-color: #64748b;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

.discussion-card {
  --card-color: #8b5cf6;
  .icon { color: var(--card-color); }
  &:hover { border-bottom: 3px solid var(--card-color); }
}

:deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.el-card__body) {
  padding: 16px 20px;
}
</style>