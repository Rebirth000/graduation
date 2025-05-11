<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>在线考试系统</span>
          <div class="card-subtitle">欢迎回来，请登录您的账户</div>
        </div>
      </template>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        status-icon
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="身份" prop="role">
          <el-select 
            v-model="loginForm.role" 
            placeholder="请选择身份" 
            style="width: 100%"
            :prefix-icon="UserFilled"
          >
            <el-option label="老师" value="ADMIN" />
            <el-option label="管理员" value="SUPER_ADMIN" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%"
            size="large"
          >
            登 录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="register-link">
            <span>没有账号？</span>
            <el-link type="primary" @click="goToRegister">学生注册</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: ''
})

const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm.username, loginForm.password, loginForm.role)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } finally {
        loading.value = false
      }
    } else {
      console.log('Form validation failed')
      return false
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  background: url('@/assets/login-bg.jfif') no-repeat center center fixed;
  background-size: cover;

  padding: 20px;
  position: relative;
}

.login-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: none;
  overflow: hidden;
  
  :deep(.el-card__header) {
    background: linear-gradient(to right, #3a7bd5, #00d2ff);
    color: white;
    padding: 25px 20px;
    border-bottom: none;
  }
}

.card-header {
  text-align: center;
  
  span {
    font-size: 1.5em;
    font-weight: 600;
    display: block;
  }
  
  .card-subtitle {
    font-size: 0.9em;
    opacity: 0.9;
    margin-top: 8px;
    font-weight: 400;
  }
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
}

.register-link {
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: #909399;
  
  .el-link {
    margin-left: 5px;
    vertical-align: baseline;
  }
}

.login-footer {
  margin-top: 30px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 1px;
}

:deep(.el-select) {
  width: 100%;
}
</style>