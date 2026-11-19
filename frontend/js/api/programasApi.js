import { httpClient } from './httpClient.js';

export const programasApi = {
  getAll: () => httpClient.get('/programas'),
  getById: (id) => httpClient.get(`/programas/${id}`),
  create: (data) => httpClient.post('/programas', data),
  update: (id, data) => httpClient.put(`/programas/${id}`, data),
  remove: (id) => httpClient.delete(`/programas/${id}`),
};
