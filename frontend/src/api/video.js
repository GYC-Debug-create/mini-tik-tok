// api/video.js — 视频管理接口
import request from '../utils/request'

// 发布视频：{ title, videoUrl }
export function publishVideo(data) {
  return request.post('/api/videos', data)
}

// 我的视频列表（分页）
export function getMyVideos(params) {
  return request.get('/api/users/me/videos', { params })
}

// 删除视频
export function deleteVideo(videoId) {
  return request.delete(`/api/videos/${videoId}`)
}

// 点赞
export function likeVideo(videoId) {
  return request.post(`/api/videos/${videoId}/like`)
}
