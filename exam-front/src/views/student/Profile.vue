<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon :size="20" class="icon"><User /></el-icon>
            <span>个人信息</span>
          </div>
          <el-button type="primary" @click="handleEdit" :icon="Edit" size="small">编辑</el-button>
        </div>
      </template>
      <div class="info-content">
        <div class="info-grid">
          <div class="info-item">
            <div class="label">
              <el-icon><User /></el-icon>
              <span>姓名</span>
            </div>
            <div class="value">{{ studentInfo.name }}</div>
          </div>
          <div class="info-item">
            <div class="label">
              <el-icon><Ticket /></el-icon>
              <span>学号</span>
            </div>
            <div class="value">{{ studentInfo.studentId }}</div>
          </div>
          <div class="info-item">
            <div class="label">
              <el-icon><Male v-if="studentInfo.gender === '男'" /><Female v-else /></el-icon>
              <span>性别</span>
            </div>
            <div class="value">{{ studentInfo.gender }}</div>
          </div>
          <div class="info-item">
            <div class="label">
              <el-icon><School /></el-icon>
              <span>班级</span>
            </div>
            <div class="value">{{ studentInfo.className }}</div>
          </div>
          <div class="info-item">
            <div class="label">
              <el-icon><Iphone /></el-icon>
              <span>联系电话</span>
            </div>
            <div class="value">{{ studentInfo.phone }}</div>
          </div>
          <div class="info-item">
            <div class="label">
              <el-icon><Message /></el-icon>
              <span>邮箱</span>
            </div>
            <div class="value">{{ studentInfo.email }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon :size="20" class="icon"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
        </div>
      </template>
      <div class="password-form-container">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          size="small"
        >
          <div class="password-form-grid">
            <el-form-item label="原密码" prop="oldPassword" class="form-item">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="原密码"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword" class="form-item">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="新密码"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="确认新密码"
                :prefix-icon="Lock"
              />
            </el-form-item>
          </div>
          <el-button 
            type="primary" 
            @click="handleChangePassword" 
            class="submit-btn"
            size="small"
          >
            修改密码
          </el-button>
        </el-form>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="编辑个人信息"
      width="500px"
      class="edit-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" :prefix-icon="Male">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="form.className" :prefix-icon="School" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" :prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" :prefix-icon="Message" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" size="small">取消</el-button>
          <el-button type="primary" @click="handleSubmit" size="small">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/authStore'
import { 
  User, Edit, Lock, Ticket, Male, Female, 
  School, Iphone, Message 
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const studentInfo = ref({})
const dialogVisible = ref(false)
const formRef = ref()
const passwordFormRef = ref()

const form = ref({
  name: '',
  gender: '',
  className: '',
  phone: '',
  email: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadStudentInfo = async () => {
  try {
    const { data } = await request({
      url: '/student/profile',
      method: 'get'
    })
    studentInfo.value = data
  } catch (error) {
    ElMessage.error('获取个人信息失败')
  }
}

const handleEdit = () => {
  form.value = { ...studentInfo.value }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      await request({
        url: '/student/profile',
        method: 'put',
        data: form.value
      })
      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadStudentInfo()
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      await request({
        url: '/student/change-password',
        method: 'post',
        data: {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        }
      })
      ElMessage.success('密码修改成功，请重新登录')
      authStore.logout()
    }
  })
}

onMounted(() => {
  loadStudentInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card, .password-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  border: none;
  
  :deep(.el-card__header) {
    background-color: #f8fafc;
    border-bottom: 1px solid #f1f5f9;
    padding: 16px 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .card-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #334155;
    
    .icon {
      margin-right: 8px;
      color: #3b82f6;
    }
  }
}

.info-content {
  padding: 16px 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8fafc;
  border-radius: 8px;
  transition: all 0.2s;
  
  &:hover {
    background-color: #f1f5f9;
    transform: translateY(-2px);
  }
  
  .label {
    display: flex;
    align-items: center;
    color: #64748b;
    font-size: 14px;
    margin-right: 12px;
    min-width: 80px;
    
    .el-icon {
      margin-right: 8px;
      color: #94a3b8;
    }
  }
  
  .value {
    font-weight: 500;
    color: #1e293b;
  }
}

.password-form-container {
  padding: 0 20px 20px;
}

.password-form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.form-item {
  margin-bottom: 0;
  
  :deep(.el-form-item__label) {
    padding-bottom: 4px;
    font-size: 12px;
    color: #64748b;
    font-weight: 500;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 6px;
    height: 32px;
  }
}

.submit-btn {
  width: 120px;
  border-radius: 6px;
  font-weight: 500;
  margin-left: auto;
  display: block;
}

.edit-dialog {
  :deep(.el-dialog__header) {
    border-bottom: 1px solid #f1f5f9;
    margin-right: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px 25px;
  }
  
  :deep(.el-dialog__footer) {
    border-top: 1px solid #f1f5f9;
    padding: 16px 20px;
  }
}

.dialog-footer {
  .el-button {
    border-radius: 6px;
    padding: 8px 16px;
  }
}
</style>