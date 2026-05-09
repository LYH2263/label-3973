import request from './request'

export const recordApi = {
  list: () => request.get('/api/records'),
  get: (id) => request.get(`/api/records/${id}`),
  create: (data) => request.post('/api/records', data),
  update: (id, data) => request.put(`/api/records/${id}`, data),
  delete: (id) => request.delete(`/api/records/${id}`)
}
