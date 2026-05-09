import request from './request'

export const constitutionApi = {
  getQuestions: () => request.get('/api/constitution/questions'),
  submit: (data) => request.post('/api/constitution/submit', data),
  getResults: () => request.get('/api/constitution/results')
}
