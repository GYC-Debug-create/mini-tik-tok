// api/feed.js — 推荐流接口
import request from '../utils/request'

// 拉新推荐
export function getNextVideo() {
  return request.get('/api/feed/videos/next')
}

// 上一条（历史记录回退）
export function getPreviousVideo(currentVideoId) {
  return request.get('/api/feed/videos/previous', {
    params: { currentVideoId }
  })
}

// 下一条（历史记录前进）
export function getForwardVideo(currentVideoId) {
  return request.get('/api/feed/videos/forward', {
    params: { currentVideoId }
  })
}
