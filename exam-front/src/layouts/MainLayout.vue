<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo-container">
        <div class="logo">
          <el-icon class="logo-icon"><Reading /></el-icon>
          <span class="logo-text">在线考试系统</span>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="menu"
        background-color="#fff"
        text-color="#5a5e66"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <template #title>
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </template>
        </el-menu-item>
        
        <!-- 管理员菜单 -->
        <template v-if="!isStudent">
          <el-sub-menu index="/announcement">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
            </template>
            <el-menu-item index="/announcement/list">公告列表</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/student/list">考生信息管理</el-menu-item>
            <el-menu-item v-if="isSuperAdmin" index="/admin/list">老师信息管理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu v-if="isSuperAdmin" index="/course-section">
            <template #title>
              <el-icon><Collection /></el-icon>
              <span>课题管理</span>
            </template>
            <el-menu-item index="/admin/course">课程管理</el-menu-item>
            <el-menu-item index="/admin/question-type">题型管理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/exam">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>考试管理</span>
            </template>
            <el-menu-item index="/exam/list">考试列表</el-menu-item>
            <el-menu-item v-if="!isSuperAdmin" index="/exam/paper">试卷管理</el-menu-item>
            <el-menu-item v-if="isSuperAdmin" index="/admin/exam-paper">试卷管理</el-menu-item>
            <el-menu-item index="/exam/history2">考试记录</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/exam-room">
            <template #title>
              <el-icon><Location /></el-icon>
              <span>考场管理</span>
            </template>
            <el-menu-item index="/exam-room/list">考场列表</el-menu-item>
          </el-sub-menu>
        </template>

        <!-- 学生菜单 -->
        <template v-else>
          <el-menu-item index="/announcements">
            <el-icon><Bell /></el-icon>
            <span>查看公告</span>
          </el-menu-item>
          
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          
          <el-menu-item index="/exam/register">
            <el-icon><Document /></el-icon>
            <span>考试报名</span>
          </el-menu-item>
          
          <el-menu-item index="/exam/my">
            <el-icon><Document /></el-icon>
            <span>我的考试</span>
          </el-menu-item>
          
          <el-menu-item index="/exam/history">
            <el-icon><Document /></el-icon>
            <span>考试记录</span>
          </el-menu-item>
        </template>

        <el-menu-item index="/discussion">
          <el-icon><ChatSquare /></el-icon>
          <span>查看讨论</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left"></div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="36" :src="avatarUrl" class="user-avatar" />
              <div class="user-details">
                <span class="user-name">{{ store.username }}</span>
                <span class="user-role">{{ userRoleText }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isStudent" @click="handleProfile" class="dropdown-item">
                  <el-icon><User /></el-icon>
                  <span>个人资料</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" class="dropdown-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div class="content-container">
          <router-view></router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { 
  HomeFilled, User, UserFilled, Document, 
  Location, Bell, ChatSquare, SwitchButton,
  Reading, ArrowDown, Collection
} from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const route = useRoute()
const router = useRouter()
const store = useAuthStore()

const avatarUrl = computed(() => {
  // 这里可以根据用户信息返回不同的头像URL
  return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})

const isSuperAdmin = computed(() => store.getUserRole === 'SUPER_ADMIN')
const isStudent = computed(() => store.getUserRole === 'STUDENT')
const userRoleText = computed(() => {
  switch (store.getUserRole) {
    case 'SUPER_ADMIN':
      return '管理员'
    case 'ADMIN':
      return '老师'
    case 'STUDENT':
      return '学生'
    default:
      return ''
  }
})

const handleLogout = () => {
  store.logout()
  router.push('/login')
}

const handleProfile = () => {
  router.push('/profile')
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  background-color: #f5f7fa;
}

.aside {
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);
  transition: all 0.3s ease;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e6e6e6;
  background-color: #ffffff;
  
  .logo {
    display: flex;
    align-items: center;
    padding: 0 16px;
    
    .logo-icon {
      font-size: 24px;
      color: #409EFF;
      margin-right: 10px;
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      white-space: nowrap;
    }
  }
}

.menu {
  border-right: none;
  padding: 8px 0;
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 48px;
    line-height: 48px;
    margin: 4px 0;
    border-radius: 4px;
    transition: all 0.3s;
    
    &:hover {
      background-color: #f0f7ff;
    }
    
    .el-icon {
      font-size: 18px;
      margin-right: 8px;
    }
  }
  
  :deep(.el-menu-item.is-active) {
    background-color: #ecf5ff;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background-color: #409EFF;
      border-radius: 0 3px 3px 0;
    }
  }
}

.header {
  height: 64px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-radius: 4px;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .user-avatar {
        margin-right: 12px;
      }
      
      .user-details {
        display: flex;
        flex-direction: column;
        
        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          line-height: 1.4;
        }
        
        .user-role {
          font-size: 12px;
          color: #909399;
          line-height: 1.4;
        }
      }
      
      .dropdown-icon {
        margin-left: 8px;
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

.main-content {
  padding: 16px;
  background-color: #f5f7fa;
  
  .content-container {
    background-color: #ffffff;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    min-height: calc(100vh - 96px);
    padding: 16px;
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  
  .el-icon {
    margin-right: 8px;
    font-size: 16px;
  }
}
</style>