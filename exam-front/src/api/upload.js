import request from '@/utils/request'

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @returns {Promise} - 返回上传结果
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/files/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 