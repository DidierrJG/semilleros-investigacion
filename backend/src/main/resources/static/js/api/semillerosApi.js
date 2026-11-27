import { request } from './httpClient.js';
import { API_BASE_URL } from '../config.js';

export const semillerosApi = {
  getAll: () => request('/semilleros'),
  getById: (id) => request(`/semilleros/${id}`),
  create: (payload) =>
    request('/semilleros', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) =>
    request(`/semilleros/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id) => request(`/semilleros/${id}`, { method: 'DELETE' }),
  addLinea: (semilleroId, lineaId) =>
    request(`/semilleros/${semilleroId}/lineas/${lineaId}`, { method: 'POST' }),
  removeLinea: (semilleroId, lineaId) =>
    request(`/semilleros/${semilleroId}/lineas/${lineaId}`, { method: 'DELETE' }),
  uploadLogo: async (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${API_BASE_URL}/semilleros/${id}/logo`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Error al subir el logo' }));
      throw new Error(err.message || 'Error al subir el logo');
    }
    return res.json();
  },
  uploadFoto: (id, file) => {
    const formData = new FormData();
    formData.append('foto', file);
    return request(`/semilleros/${id}/foto`, {
      method: 'POST',
      body: formData,
    });
  },
  removeFoto: (id) => request(`/semilleros/${id}/foto`, { method: 'DELETE' }),
  enviarCorreo: (id, payload) =>
    request(`/semilleros/${id}/correo`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  exportarActualizacion: async (id) => {
    let response;
    try {
      response = await fetch(`${API_BASE_URL}/semilleros/${id}/exportar-actualizacion`);
    } catch {
      throw new Error('No se pudo conectar con el servidor backend para descargar el archivo.');
    }

    if (!response.ok) {
      let errorMessage = `Error al generar el formato Excel (HTTP ${response.status})`;
      try {
        const errorJson = await response.json();
        if (errorJson && errorJson.message) {
          errorMessage = errorJson.message;
        }
      } catch {
        // Fallback a mensaje estándar
      }
      throw new Error(errorMessage);
    }

    const blob = await response.blob();
    const disposition = response.headers.get('Content-Disposition');
    let filename = `Actualizacion_Semillero_${id}.xlsx`;

    if (disposition) {
      const utf8Match = disposition.match(/filename\*=UTF-8''([^;\r\n]+)/i);
      if (utf8Match && utf8Match[1]) {
        filename = decodeURIComponent(utf8Match[1].trim());
      } else {
        const standardMatch = disposition.match(/filename="?([^";\r\n]+)"?/i);
        if (standardMatch && standardMatch[1]) {
          filename = standardMatch[1].trim();
        }
      }

      // Decodificar si viene con formato RFC 2047 Q-encoding (=?UTF-8?Q?...?=)
      if (filename.includes('=?') || filename.includes('?=')) {
        filename = filename
          .replace(/=\?[^?]+\?[qQ]\?/i, '')
          .replace(/\?=/g, '')
          .replace(/=([0-9A-Fa-f]{2})/g, (_, hex) => String.fromCharCode(parseInt(hex, 16)));
      }
    }

    return { blob, filename };
  },
};

