import axios from "axios";

const configuredApiUrl = import.meta.env.VITE_API_BASE_URL;
const apiBaseUrl = configuredApiUrl
  ? `${configuredApiUrl.replace(/\/$/, "").replace(/\/api$/, "")}/api`
  : "/api";

export const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 15000,
});

// API records use paths such as /api/incidents/uploads/photo.webp.  Resolve
// those paths against the API host so a separately deployed Vercel frontend
// does not try to load them from its own domain.
export function resolveApiUrl(path) {
  if (!path || /^https?:\/\//i.test(path)) return path;
  const apiOrigin = apiBaseUrl.replace(/\/api\/?$/, "");
  return `${apiOrigin}${path.startsWith("/") ? path : `/${path}`}`;
}

// Add JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("pahadsuraksha_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Clear token on 401 (unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("pahadsuraksha_token");
    }
    return Promise.reject(error);
  }
);

export const USE_MOCK = import.meta.env.VITE_USE_MOCK_DATA === "true";
