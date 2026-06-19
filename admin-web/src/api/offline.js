import request from '../utils/request'

export function fetchOfflineActivities(status) {
  return request({
    url: '/api/offline/activities',
    method: 'get',
    params: { status }
  })
}

export function createOfflineActivity(data) {
  return request({
    url: '/api/offline/activities',
    method: 'post',
    data
  })
}

export function createOfflineBooking(activityId) {
  return request({
    url: '/api/offline/bookings',
    method: 'post',
    data: { activityId }
  })
}

export function fetchMyOfflineBookings() {
  return request({
    url: '/api/offline/bookings/my',
    method: 'get'
  })
}

export function fetchAllOfflineBookings() {
  return request({
    url: '/api/offline/bookings/all',
    method: 'get'
  })
}

export function updateOfflineBookingStatus(id, status) {
  return request({
    url: `/api/offline/bookings/${id}/status`,
    method: 'put',
    data: { status }
  })
}
