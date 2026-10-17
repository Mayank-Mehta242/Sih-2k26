export const TOKEN_KEY = 'hr_token';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  (typeof window !== 'undefined'
    ? `${window.location.origin}/api`
    : 'http://localhost:8080');

const USE_MOCKS = import.meta.env.VITE_USE_MOCKS === 'true';
const NODES_API_BASE_URL =
  import.meta.env.VITE_NODES_API_BASE_URL ?? API_BASE_URL;

export class ApiError extends Error {
  constructor(message, status = 0, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

function getAuthHeaders() {
  const token = localStorage.getItem(TOKEN_KEY);
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function simulateDelay() {
  const ms = Math.floor(Math.random() * 500) + 300;
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function apiFetch(
  path,
  { method = 'GET', body, headers = {}, ...options } = {}
) {
  const url = path.startsWith('http') ? path : `${API_BASE_URL}${path}`;
  let response;
  try {
    response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
        ...headers,
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      ...options,
    });
  } catch {
    throw new ApiError(
      'Unable to reach the server. Check your connection and try again.'
    );
  }

  const contentType = response.headers.get('content-type') ?? '';
  const payload = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : null;

  if (!response.ok) {
    throw new ApiError(
      payload?.message ?? `Request failed with status ${response.status}.`,
      response.status,
      payload
    );
  }
  return payload;
}

export async function getStations() {
  return apiFetch('/stations');
}

export async function getStationData(stationId, dataType) {
  const query = dataType ? `?type=${encodeURIComponent(dataType)}` : '';
  return apiFetch(`/stations/${encodeURIComponent(stationId)}/data${query}`);
}

export async function getNodes() {
  return apiFetch(`${NODES_API_BASE_URL}/nodes/getAllNodes`);
}

export async function getNodeByName(name) {
  return apiFetch(
    `${NODES_API_BASE_URL}/nodes/getbyName/${encodeURIComponent(name)}`
  );
}

export async function addNode(data) {
  return apiFetch(`${NODES_API_BASE_URL}/nodes/addNode`, {
    method: 'POST',
    body: data,
  });
}

export async function deleteNode(id) {
  return apiFetch(
    `${NODES_API_BASE_URL}/nodes/deleteNode/${encodeURIComponent(id)}`,
    { method: 'DELETE' }
  );
}

export async function getRoadSegments() {
  return apiFetch('/road-segments');
}

export async function getRoadSegmentRisk(id) {
  return apiFetch(`/road-segments/${encodeURIComponent(id)}/risk`);
}

export async function assessRouteRisk(segmentIds) {
  return apiFetch('/road-segments/risk/assess', {
    method: 'POST',
    body: { segmentIds },
  });
}

export async function submitCitizenReport(formData) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/landslide-report/makeReport`, {
      method: 'POST',
      headers: { ...getAuthHeaders() },
      body: formData,
    });
  } catch {
    throw new ApiError(
      'Unable to reach the server. Check your connection and try again.'
    );
  }

  const contentType = response.headers.get('content-type') ?? '';
  const payload = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : null;

  if (!response.ok) {
    throw new ApiError(
      payload?.message ?? `Report failed with status ${response.status}.`,
      response.status,
      payload
    );
  }
  return payload;
}

export { API_BASE_URL, NODES_API_BASE_URL, USE_MOCKS };
