import axios, { AxiosInstance } from 'axios';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://192.168.200.100:3000/api/v1';

export const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
});

// Attach Firebase ID token to every request
api.interceptors.request.use(async (config) => {
  try {
    const { auth } = await import('./firebase');
    const user = auth?.currentUser;
    if (user) {
      const token = await user.getIdToken();
      config.headers.Authorization = `Bearer ${token}`;
    }
  } catch {
    // Not authenticated yet
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const message = err.response?.data?.message ?? err.message ?? 'Error desconocido';
    return Promise.reject(new Error(message));
  },
);

// ─── Reports ──────────────────────────────────────────────────────────────────
export const reportsApi = {
  list: (params?: object) => api.get('/reports', { params }).then((r) => r.data),
  get: (id: string) => api.get(`/reports/${id}`).then((r) => r.data),
  updateStatus: (id: string, status: string, reason?: string) =>
    api.patch(`/reports/${id}/status`, { status, reason }).then((r) => r.data),
  delete: (id: string) => api.delete(`/reports/${id}`).then((r) => r.data),
  analyze: (id: string) => api.post(`/reports/${id}/analyze`).then((r) => r.data),
};

// ─── Users ────────────────────────────────────────────────────────────────────
export const usersApi = {
  list: (params?: object) => api.get('/users', { params }).then((r) => r.data),
  get: (id: string) => api.get(`/users/${id}`).then((r) => r.data),
  updateRole: (id: string, role: string) => api.patch(`/users/${id}/role`, { role }).then((r) => r.data),
  updateStatus: (id: string, status: string) => api.patch(`/users/${id}/status`, { status }).then((r) => r.data),
};

// ─── Alerts ───────────────────────────────────────────────────────────────────
export const alertsApi = {
  listAll: () => api.get('/alerts/all').then((r) => r.data),
  get: (id: string) => api.get(`/alerts/${id}`).then((r) => r.data),
  create: (data: object) => api.post('/alerts', data).then((r) => r.data),
  deactivate: (id: string) => api.patch(`/alerts/${id}/deactivate`).then((r) => r.data),
};

// ─── Risk Zones ───────────────────────────────────────────────────────────────
export const riskZonesApi = {
  list: () => api.get('/risk-zones').then((r) => r.data),
  get: (id: string) => api.get(`/risk-zones/${id}`).then((r) => r.data),
  create: (data: object) => api.post('/risk-zones', data).then((r) => r.data),
  update: (id: string, data: object) => api.patch(`/risk-zones/${id}`, data).then((r) => r.data),
  delete: (id: string) => api.delete(`/risk-zones/${id}`).then((r) => r.data),
};

// ─── Missing Persons ──────────────────────────────────────────────────────────
export const missingPersonsApi = {
  list: () => api.get('/missing-persons').then((r) => r.data),
  get: (id: string) => api.get(`/missing-persons/${id}`).then((r) => r.data),
  updateStatus: (id: string, status: string) =>
    api.patch(`/missing-persons/${id}/status`, { status }).then((r) => r.data),
};

// ─── Statistics ───────────────────────────────────────────────────────────────
export const statisticsApi = {
  dashboard: () => api.get('/statistics/dashboard').then((r) => r.data),
  trends: (days?: number) => api.get('/statistics/trends', { params: { days } }).then((r) => r.data),
};
