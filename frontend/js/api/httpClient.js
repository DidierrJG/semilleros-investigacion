import { API_BASE_URL } from '../config.js';

export class ApiError extends Error {
  constructor(message, status, fieldErrors = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function parseBody(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

export async function request(path, options = {}) {
  const url = `${API_BASE_URL}${path}`;
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
  const headers = { ...(options.headers || {}) };
  if (!isFormData && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  const config = {
    headers,
    ...options,
  };

  let response;
  try {
    response = await fetch(url, config);
  } catch {
    throw new ApiError(
      'No se pudo conectar con el servidor backend (http://localhost:8080). Verifica que Spring Boot esté ejecutándose.',
      0
    );
  }

  const data = await parseBody(response);

  if (!response.ok) {
    const fieldErrors = data?.errors || null;
    let message = data?.message || `Error HTTP ${response.status}`;
    if (fieldErrors && typeof fieldErrors === 'object') {
      const details = Object.values(fieldErrors).join(' · ');
      if (details) message = `${message}: ${details}`;
    }
    throw new ApiError(message, response.status, fieldErrors);
  }

  return data;
}

export const httpClient = {
  get: (path, options) => request(path, { method: 'GET', ...options }),
  post: (path, body, options) =>
    request(path, { method: 'POST', body: JSON.stringify(body), ...options }),
  put: (path, body, options) =>
    request(path, { method: 'PUT', body: JSON.stringify(body), ...options }),
  delete: (path, options) => request(path, { method: 'DELETE', ...options }),
};


