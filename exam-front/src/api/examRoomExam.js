import request from '@/utils/request'

/**
 * 根据考场ID获取关联的考试信息
 * @param {number} examRoomId - 考场ID
 */
export function getExamsByRoomId(examRoomId) {
  return request({
    url: `/exam-room-exam/room/${examRoomId}`,
    method: 'get'
  })
}

/**
 * 根据考试ID获取关联的考场信息
 * @param {number} examId - 考试ID
 */
export function getRoomsByExamId(examId) {
  return request({
    url: `/exam-room-exam/exam/${examId}`,
    method: 'get'
  })
}

/**
 * 获取单个考场考试关联信息
 * @param {number} id - 关联记录ID
 */
export function getExamRoomExamById(id) {
  return request({
    url: `/exam-room-exam/${id}`,
    method: 'get'
  })
}

/**
 * 添加考场考试关联
 * @param {Object} data - 考场考试关联信息
 * @param {number} data.examRoomId - 考场ID
 * @param {number} data.examId - 考试ID
 */
export function addExamRoomExam(data) {
  return request({
    url: '/exam-room-exam',
    method: 'post',
    data
  })
}

/**
 * 更新考场考试关联
 * @param {number} id - 关联记录ID
 * @param {Object} data - 考场考试关联信息
 * @param {number} data.examRoomId - 考场ID
 * @param {number} data.examId - 考试ID
 */
export function updateExamRoomExam(id, data) {
  return request({
    url: `/exam-room-exam/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除考场考试关联
 * @param {number} id - 关联记录ID
 */
export function deleteExamRoomExam(id) {
  return request({
    url: `/exam-room-exam/${id}`,
    method: 'delete'
  })
}

/**
 * 删除考场的所有关联考试
 * @param {number} examRoomId - 考场ID
 */
export function deleteExamsByRoomId(examRoomId) {
  return request({
    url: `/exam-room-exam/room/${examRoomId}`,
    method: 'delete'
  })
}

/**
 * 删除考试的所有关联考场
 * @param {number} examId - 考试ID
 */
export function deleteRoomsByExamId(examId) {
  return request({
    url: `/exam-room-exam/exam/${examId}`,
    method: 'delete'
  })
} 