<template>
  <div class="discussion-detail">
    <div v-loading="loading" class="main-content">
      <!-- 讨论内容 -->
      <el-card class="content-card">
        <div class="header">
          <h2>{{ discussion.title }}</h2>
          <div class="info">
            <el-tag size="small" type="primary">创建人：{{ discussion.creatorName }}</el-tag>
            <el-tag size="small" type="info">创建时间：{{ dayjs(discussion.createdAt).format('YYYY-MM-DD HH:mm:ss') }}</el-tag>
            <el-tag 
              size="small" 
              :type="isExpired ? 'danger' : 'success'"
            >
              截止时间：{{ dayjs(discussion.deadline).format('YYYY-MM-DD HH:mm:ss') }}
            </el-tag>
          </div>
        </div>
        <div class="content" v-html="discussion.content"></div>
      </el-card>

      <!-- 回复列表 -->
      <el-card class="reply-card">
        <div class="reply-header">
          <h3>回复列表 ({{ replies.length }})</h3>
          <el-button
            v-if="!isExpired"
            type="primary"
            @click="handleAddReply"
            round
          >
            添加回复
          </el-button>
        </div>

        <div v-if="replies.length === 0" class="no-reply">
          暂无回复
        </div>

        <div v-else class="reply-list">
          <div v-for="reply in replies" :key="reply.id" class="reply-item">
            <div class="reply-info">
              <div class="left">
                <span class="author">{{ reply.creatorName }}</span>
                <span class="time">{{ dayjs(reply.createdAt).format('YYYY-MM-DD HH:mm:ss') }}</span>
              </div>
              <div v-if="canEdit(reply)" class="actions">
                <template v-if="!reply.editing">
                  <el-button v-if="!(reply.creatorRole === 'STUDENT' && userRole !== 'STUDENT')" link type="primary" @click="handleEditReply(reply)" round>编辑</el-button>
                  <el-button link type="danger" @click="handleDeleteReply(reply)" round>删除</el-button>
                </template>
                <template v-else>
                  <el-button link type="primary" @click="saveReply(reply)" round>保存</el-button>
                  <el-button link @click="cancelEdit(reply)" round>取消</el-button>
                </template>
              </div>
            </div>
            <div class="reply-content">
              <template v-if="!reply.editing">
                <div v-html="reply.content"></div>
              </template>
              <div v-else class="editor-container">
                <Toolbar
                  style="border-bottom: 1px solid #ccc"
                  :editor="editorRef"
                  :defaultConfig="toolbarConfig"
                  mode="default"
                />
                <Editor
                  style="height: 300px; overflow-y: hidden;"
                  v-model="reply.editContent"
                  :defaultConfig="editorConfig"
                  mode="default"
                  @onCreated="handleEditorCreated"
                />
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 添加回复对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加回复"
      width="800px"
      class="reply-dialog"
    >
      <el-form
        ref="replyFormRef"
        :model="replyForm"
        :rules="rules"
        label-width="0"
      >
        <el-form-item prop="content">
          <div class="editor-container">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 300px; overflow-y: hidden;"
              v-model="replyForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="submitReply" round>确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, shallowRef, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiscussionById, getDiscussionReplies, addReply, updateReply, deleteReply } from '@/api/discussion'
import dayjs from 'dayjs'
import { useAuthStore } from '../../stores/authStore'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const route = useRoute()
const store = useAuthStore()
const loading = ref(false)
const discussion = ref({})
const replies = ref([])
const dialogVisible = ref(false)
const replyFormRef = ref()

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

// 内容 HTML
const replyForm = ref({
  content: ''
})

// 工具栏配置
const toolbarConfig = {
  excludeKeys: [
    'uploadVideo',
    'insertTable',
    'codeBlock',
    'todo'
  ]
}

// 编辑器配置
const editorConfig = {
  placeholder: '请输入回复内容...',
  MENU_CONF: {}
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

const rules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const isExpired = computed(() => {
  return discussion.value.deadline && dayjs(discussion.value.deadline).isBefore(dayjs())
})

const userId = computed(() => store.getUserId)
const userRole = computed(() => store.getUserRole)

const loadData = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const [discussionRes, repliesRes] = await Promise.all([
      getDiscussionById(id),
      getDiscussionReplies(id)
    ])
    discussion.value = discussionRes.data
    replies.value = repliesRes.data
  } catch (error) {
    console.error('获取讨论详情失败:', error)
    ElMessage.error('获取讨论详情失败')
  } finally {
    loading.value = false
  }
}

const handleAddReply = () => {
  if (isExpired.value) {
    ElMessage.warning('讨论已截止，不能回复')
    return
  }
  replyForm.value.content = ''
  dialogVisible.value = true
  nextTick(() => {
    replyFormRef.value?.clearValidate()
  })
}

const submitReply = async () => {
  if (!replyFormRef.value) return
  await replyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await addReply(discussion.value.id, {
          content: replyForm.value.content,
          creatorRole: userRole.value === 'STUDENT' ? 'STUDENT' : 'ADMIN'
        })
        dialogVisible.value = false
        ElMessage.success('回复成功')
        loadData()
      } catch (error) {
        console.error('添加回复失败:', error)
      }
    }
  })
}
const canEdit = (reply) => {
  if (isExpired.value) return false
  return userRole.value !== 'STUDENT' || reply.createdBy === Number(userId.value) && reply.creatorRole === 'STUDENT'
}

const handleEditReply = (reply) => {
  reply.editing = true
  reply.editContent = reply.content
}

const saveReply = async (reply) => {
  if (!reply.editContent?.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  try {
    await updateReply(reply.id, {
      content: reply.editContent,
      discussionId: discussion.value.id
    })
    reply.content = reply.editContent
    reply.editing = false
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新回复失败:', error)
    ElMessage.error('更新回复失败')
  }
}

const cancelEdit = (reply) => {
  reply.editing = false
  reply.editContent = reply.content
}

const handleDeleteReply = (reply) => {
  ElMessageBox.confirm('确认删除该回复吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReply(reply.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除回复失败:', error)
      ElMessage.error('删除回复失败')
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.discussion-detail {
  padding: 20px;
  background: #f6f8fa;
  min-height: 100vh;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-card {
  border-radius: 10px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.08);
  background: #fff;
}

.header {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.header h2 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 1.5rem;
  font-weight: bold;
}

.info {
  display: flex;
  gap: 10px;
}

.content {
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  padding: 10px 0;
}

.reply-card {
  border-radius: 10px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.08);
  background: #fff;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.reply-header h3 {
  margin: 0;
  color: #303133;
  font-size: 1.2rem;
  font-weight: bold;
}

.no-reply {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reply-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  transition: background-color 0.3s;
}

.reply-item:hover {
  background-color: #f0f7ff;
  border-radius: 8px;
  padding: 10px;
}

.reply-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 13px;
}

.reply-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  padding: 0 10px;
}

.actions {
  display: flex;
  gap: 10px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}

.reply-dialog >>> .el-dialog {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.w-e-text-container) {
  min-height: 300px !important;
}

:deep(.w-e-toolbar) {
  border-top: none !important;
  border-left: none !important;
  border-right: none !important;
}

:deep(.w-e-bar-item) {
  border-radius: 4px;
}

:deep(.w-e-bar-item:hover) {
  background-color: #f0f7ff;
}
</style> 