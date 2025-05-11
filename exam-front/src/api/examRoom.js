import request from '@/utils/request'

/**
   * 获取考场列表
 * @param {number} page - 当前页码
 * @param {number} size - 每页条数
 */
export function getList(page, size) {
  return request({
    url: '/exam-room/list',
    method: 'get',
    params: {
      page,
      size
    }
  })
}
/**
 * 获取考场列表
 */
export function getExamRoomList() {
  return request({
    url: '/exam-room',
    method: 'get'
  })
}

/**
 * 获取考场详情
 * @param {number} id - 考场ID
 */
export function getExamRoomById(id) {
  return request({
    url: `/exam-room/${id}`,
    method: 'get'
  })
}

/**
 * 添加考场
 * @param {Object} data - 考场信息
 */
export function addExamRoom(data) {
  return request({
    url: '/exam-room',
    method: 'post',
    data
  })
}

/**
 * 更新考场
 * @param {number} id - 考场ID
 * @param {Object} data - 考场信息
 */
export function updateExamRoom(id, data) {
  return request({
    url: `/exam-room/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除考场
 * @param {number} id - 考场ID
 */
export function deleteExamRoom(id) {
  return request({
    url: `/exam-room/${id}`,
    method: 'delete'
  })
} 