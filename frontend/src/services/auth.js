const AUTH_STORAGE_KEY = "chickenwiki.auth";

async function requestJson(path, options = {}) {
  const res = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  if (!res.ok) {
    let message = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      message = body.detail || body.message || body.error || message;
    } catch {
      // Keep the HTTP status message when the backend returns an empty body.
    }
    throw new Error(message);
  }

  return res.json();
}

export function getStoredAuth() {
  const saved = localStorage.getItem(AUTH_STORAGE_KEY) || sessionStorage.getItem(AUTH_STORAGE_KEY);
  if (!saved) return null;

  try {
    return JSON.parse(saved);
  } catch {
    clearStoredAuth();
    return null;
  }
}

export function saveStoredAuth(auth, remember) {
  clearStoredAuth();
  const storage = remember ? localStorage : sessionStorage;
  storage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
}

export function clearStoredAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  sessionStorage.removeItem(AUTH_STORAGE_KEY);
}

export async function loginRequest(payload) {
  return requestJson("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function signupRequest(payload) {
  return requestJson("/api/auth/signup", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function meRequest(accessToken) {
  return requestJson("/api/auth/me", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}
