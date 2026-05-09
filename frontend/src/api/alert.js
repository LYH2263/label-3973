import request from './request'

export const alertApi = {
  list: (recordId) => request.get('/api/alerts', { params: { recordId } })
}
