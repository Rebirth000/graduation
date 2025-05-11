import request from '@/utils/request'

/**
 * 获取管理员列表
 * @param {Object} params - 分页参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 */
export function getAdminList(params) {
  return request({
    url: '/admin/list',
    method: 'get',
    params
  })
}

export function addAdmin(data) {
  return request({
    url: '/admin',
    method: 'post',
    data
  })
}

export function deleteAdmin(id) {
  return request({
    url: `/admin/${id}`,
    method: 'delete'
  })
}

export function resetPassword(id) {
  return request({
    url: `/admin/${id}/reset-password`,
    method: 'post'
  })
} 