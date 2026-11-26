import { request } from './httpClient.js';

export const capacitacionesApi = {
  getAll: () => request('/capacitaciones'),
  getBySemilleroId: (semilleroId) => request(`/capacitaciones/semillero/${semilleroId}`),
  getById: (id) => request(`/capacitaciones/${id}`),
  create: (payload) =>
    request('/capacitaciones', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) =>
    request(`/capacitaciones/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id) => request(`/capacitaciones/${id}`, { method: 'DELETE' }),
};

