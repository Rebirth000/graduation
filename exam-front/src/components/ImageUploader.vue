<template>
  <div class="image-uploader">
    <el-upload
      class="uploader"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="customUpload"
      :disabled="disabled"
    >
      <div v-if="modelValue" class="image-preview">
        <img :src="modelValue" class="uploaded-image" />
        <div class="image-actions">
          <el-button type="primary" size="small" icon="el-icon-zoom-in" circle @click.stop="previewImage"></el-button>
          <el-button type="danger" size="small" icon="el-icon-delete" circle @click.stop="removeImage"></el-button>
        </div>
      </div>
      <el-icon v-else class="uploader-icon"><Plus /></el-icon>
    </el-upload>

    <el-dialog v-model="previewVisible" title="图片预览">
      <img :src="modelValue" alt="预览图片" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const previewVisible = ref(false)

const beforeUpload = (file) => {
  // 检查文件类型
  const isImage = /^image\/(jpeg|png|gif|jpg)$/.test(file.type)
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  // 检查文件大小
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  return true
}

const customUpload = async (options) => {
  try {
    const { data: imageUrl } = await uploadImage(options.file)
    emit('update:modelValue', imageUrl)
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
    console.error('上传失败:', error)
  }
}

const previewImage = (e) => {
  e.stopPropagation()
  previewVisible.value = true
}

const removeImage = (e) => {
  e.stopPropagation()
  emit('update:modelValue', '')
  ElMessage.success('图片已移除')
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 240px;
  height: 160px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
  background-color: #fafafa;
}

.uploader:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 8px;
}

.image-preview:hover .image-actions {
  opacity: 1;
}

:deep(.el-dialog__body) {
  text-align: center;
  padding: 20px;
  
  img {
    max-width: 100%;
    max-height: 70vh;
    object-fit: contain;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}
</style> 