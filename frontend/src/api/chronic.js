import request from './request'

export const chronicApi = {
  list: (recordId) => request.get('/api/chronic-diseases', { params: { recordId } }),
  create: (data) => request.post('/api/chronic-diseases', data),
  update: (id, data) => request.put(`/api/chronic-diseases/${id}`, data),
  delete: (id) => request.delete(`/api/chronic-diseases/${id}`)
}
