import request from '@/utils/request'

export function getStudentList(params) {
  return request({
    url: '/student/list',
    method: 'get',
    params
  })
}

export function addStudent(data) {
  return request({
    url: '/student',
    method: 'post',
    data
  })
}

export function batchImportStudents(data) {
  return request({
    url: '/student/batch-import',
    method: 'post',
    data
  })
}

export function updateStudent(id, data) {
  return request({
    url: `/student/${id}`,
    method: 'put',
    data
  })
}

export function deleteStudent(id) {
  return request({
    url: `/student/${id}`,
    method: 'delete'
  })
}

export function resetPassword(id) {
  return request({
    url: `/student/${id}/reset-password`,
    method: 'post'
  })
}


