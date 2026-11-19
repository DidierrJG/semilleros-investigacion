import { request } from './httpClient.js';

export const gruposInvestigacionApi = {
  getAll: () => request('/grupos-investigacion'),
  getById: (id) => request(`/grupos-investigacion/${id}`),
  create: (payload) =>
    request('/grupos-investigacion', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) =>
    request(`/grupos-investigacion/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id) => request(`/grupos-investigacion/${id}`, { method: 'DELETE' }),
};
