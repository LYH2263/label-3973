import request from './request'

export const indicatorApi = {
  list: (recordId) => request.get('/api/indicators', { params: { recordId } }),
  create: (data) => request.post('/api/indicators', data),
  delete: (id) => request.delete(`/api/indicators/${id}`),
  listAlerts: (recordId) => request.get('/api/alerts', { params: { recordId } })
}
