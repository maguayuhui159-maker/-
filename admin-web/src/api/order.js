import request from '../utils/request'

export function createOrder(workId) {
  return request({
    url: '/api/orders',
    method: 'post',
    data: { workId }
  })
}

export function fetchMyOrders() {
  return request({
    url: '/api/orders/my',
    method: 'get'
  })
}

export function fetchAllOrders(params) {
  return request({
    url: '/api/orders/all',
    method: 'get',
    params
  })
}

export function updateOrderStatus(id, status) {
  return request({
    url: `/api/orders/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function applyAfterSale(id, data) {
  return request({
    url: `/api/orders/${id}/aftersale`,
    method: 'post',
    data
  })
}

export function fetchAfterSaleList() {
  return request({
    url: '/api/orders/aftersale/all',
    method: 'get'
  })
}

export function reviewAfterSale(id, data) {
  return request({
    url: `/api/orders/aftersale/${id}/review`,
    method: 'put',
    data
  })
}
