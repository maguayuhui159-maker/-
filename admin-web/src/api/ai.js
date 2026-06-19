import request from '../utils/request'

export function getAiReply(data) {
  return request({
    url: '/api/ai/chat',
    method: 'post',
    data: data,
    // AI 推理可能超过默认 5s，单独放宽超时时间
    timeout: 60000
  })
}

export function generateAiImage(data) {
  return request({
    url: '/api/ai/image-generate',
    method: 'post',
    data: data,
    timeout: 120000
  })
}
