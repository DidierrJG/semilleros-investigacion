import { request } from './httpClient.js';

export const programasApi = {
  getAll: () => request('/programas'),
  getById: (id) => request(`/programas/${id}`),
  create: (payload) =>
    request('/programas', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) =>
    request(`/programas/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id) => request(`/programas/${id}`, { method: 'DELETE' }),
};
