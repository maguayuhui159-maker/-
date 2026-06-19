import request from '../utils/request'

export function fetchCourseList() {
  return request({
    url: '/api/courses',
    method: 'get'
  })
}

export function fetchAllCourseList() {
  return request({
    url: '/api/courses/all',
    method: 'get'
  })
}

export function fetchMyUploadedCourses() {
  return request({
    url: '/api/courses/my-uploaded',
    method: 'get'
  })
}

export function uploadCourse(data) {
  return request({
    url: '/api/courses/upload',
    method: 'post',
    data
  })
}

export function uploadCourseVideo(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/files/upload/video',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 180000
  })
}

export function purchaseCourse(courseId) {
  return request({
    url: '/api/learning/purchase',
    method: 'post',
    data: { courseId }
  })
}

export function fetchMyLearningCourses() {
  return request({
    url: '/api/learning/my-courses',
    method: 'get'
  })
}

export function updateLearningProgress(data) {
  return request({
    url: '/api/learning/progress',
    method: 'put',
    data
  })
}

export function submitHomework(data) {
  return request({
    url: '/api/learning/homework',
    method: 'post',
    data
  })
}

export function fetchMyHomework() {
  return request({
    url: '/api/learning/homework/my',
    method: 'get'
  })
}

export function fetchAllHomework(status) {
  return request({
    url: '/api/learning/homework/all',
    method: 'get',
    params: { status }
  })
}

export function reviewHomework(id, data) {
  return request({
    url: `/api/learning/homework/${id}/review`,
    method: 'put',
    data
  })
}
