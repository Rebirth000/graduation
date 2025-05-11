<template>
  <div class="exam-paper-add">
    <el-form :model="paperForm" :rules="rules" ref="paperFormRef" label-width="100px">
      <el-form-item label="试卷标题" prop="title">
        <el-input v-model="paperForm.title" placeholder="请输入试卷标题" />
      </el-form-item>
      
      <el-form-item label="所属课程" prop="courseId">
        <el-select v-model="paperForm.courseId" placeholder="请选择课程" style="width: 100%">
          <el-option
            v-for="course in courseList"
            :key="course.id"
            :label="course.name"
            :value="course.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="试卷描述" prop="description">
        <el-input
          type="textarea"
          v-model="paperForm.description"
          placeholder="请输入试卷描述"
          :rows="4"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createExamPaper } from '@/api/exam'
import { getAllCourses } from '@/api/course'

export default {
  name: 'ExamPaperAdd',
  setup() {
    const router = useRouter()
    const paperFormRef = ref(null)
    const courseList = ref([])

    const paperForm = reactive({
      title: '',
      description: '',
      courseId: ''
    })

    const rules = {
      title: [
        { required: true, message: '请输入试卷标题', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      courseId: [
        { required: true, message: '请选择所属课程', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入试卷描述', trigger: 'blur' }
      ]
    }

    // 获取课程列表
    const fetchCourseList = async () => {
      try {
        const response = await getAllCourses()
        courseList.value = response.data
      } catch (error) {
        ElMessage.error('获取课程列表失败')
      }
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!paperFormRef.value) return
      
      await paperFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            await createExamPaper(paperForm)
            ElMessage.success('创建试卷成功')
            router.push('/admin/exam-paper')
          } catch (error) {
            ElMessage.error('创建试卷失败')
          }
        }
      })
    }

    onMounted(() => {
      fetchCourseList()
    })

    return {
      paperForm,
      paperFormRef,
      courseList,
      rules,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.exam-paper-add {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style> 