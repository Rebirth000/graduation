<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>在线考试系统</span>
          <div class="card-subtitle">创建您的学生账户</div>
        </div>
      </template>
      <el-form
        ref="registerFormRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        status-icon
        @submit.prevent="handleRegister"
      >
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入真实姓名" 
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input 
            v-model="form.studentId" 
            placeholder="请输入学号" 
            clearable
            :prefix-icon="Avatar"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select 
            v-model="form.gender" 
            placeholder="请选择性别" 
            style="width: 100%"
            :prefix-icon="Male"
          >
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input 
            v-model="form.className" 
            placeholder="请输入班级名称" 
            clearable
            :prefix-icon="School"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%"
            size="large"
          >
            注 册
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="login-link">
            <span>已有账号？</span>
            <el-link type="primary" @click="goToLogin">立即登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
    <div class="register-footer">
      <span>© 2023 在线考试系统 版权所有</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { addStudent } from '@/api/student'
import { User, Lock, Avatar, Male, School } from '@element-plus/icons-vue'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const form = ref({
  name: '',
  studentId: '',
  password: '',
  confirmPassword: '',
  gender: '',
  className: ''
})

// 自定义密码验证规则
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.value.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = reactive({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentId: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 6, message: '账号长度不能少于6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }]
})

// 处理注册提交
const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await addStudent(form.value)
        ElMessage.success('注册成功')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  position: relative;
}

.register-card {
  width: 480px;
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

.login-link {
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: #909399;
  
  .el-link {
    margin-left: 5px;
    vertical-align: baseline;
  }
}

.register-footer {
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