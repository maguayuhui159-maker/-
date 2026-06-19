import request from '../utils/request'

export function fetchWorkList() {
  return request({
    url: '/api/works',
    method: 'get'
  })
}

export function fetchAllWorkList() {
  return request({
    url: '/api/works/all',
    method: 'get'
  })
}

export function fetchMyWorkList() {
  return request({
    url: '/api/works/my',
    method: 'get'
  })
}

export function uploadWork(data) {
  return request({
    url: '/api/works/upload',
    method: 'post',
    data
  })
}

export function favoriteWork(id) {
  return request({
    url: `/api/works/${id}/favorite`,
    method: 'post'
  })
}

export function unfavoriteWork(id) {
  return request({
    url: `/api/works/${id}/favorite`,
    method: 'delete'
  })
}

export function fetchMyFavoriteWorks() {
  return request({
    url: '/api/works/favorites/my',
    method: 'get'
  })
}

export function recordWorkView(id) {
  return request({
    url: `/api/works/${id}/view`,
    method: 'post'
  })
}

export function fetchMyRecentViewedWorks() {
  return request({
    url: '/api/works/recent-viewed/my',
    method: 'get'
  })
}
