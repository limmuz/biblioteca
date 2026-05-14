import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('lybre_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config?.url?.includes('/auth/');
    if (error.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem('lybre_token');
      localStorage.removeItem('lybre_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;