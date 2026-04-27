export function isAuthenticated() {
  return Boolean(localStorage.getItem('lybre_token'));
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem('lybre_user')) || {};
  } catch {
    return {};
  }
}

export function saveSession(authResponse) {
  localStorage.setItem('lybre_token', authResponse.token);
  localStorage.setItem(
    'lybre_user',
    JSON.stringify({ nome: authResponse.nome, email: authResponse.email })
  );
}

export function clearSession() {
  localStorage.removeItem('lybre_token');
  localStorage.removeItem('lybre_user');
}