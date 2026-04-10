const Auth = (() => {
  const KEYCLOAK_BASE = 'http://localhost:5053/realms/my-finance-app/protocol/openid-connect';
  const CLIENT_ID = 'my-finance-app';
  const CLIENT_SECRET = '2s08ho5l7WetTF6ZLmNASYiHCH8UpPdL'; 
  const REDIRECT_URI = window.location.origin + window.location.pathname;

  const STORAGE_KEYS = {
    TOKEN: 'myfinance_token', // Sincronizado com API_BASE
    REFRESH: 'mf_refresh_token',
    EXPIRES: 'mf_token_expires',
    CODE_VER: 'mf_code_verifier',
    STATE: 'mf_oauth_state',
  };

  const _crypto = {
    generateRandomString: (len = 64) => {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
      return Array.from(crypto.getRandomValues(new Uint8Array(len))).map(b => chars[b % chars.length]).join('');
    },
    sha256: async (plain) => {
      const encoder = new TextEncoder();
      const data = encoder.encode(plain);
      return await crypto.subtle.digest('SHA-256', data);
    },
    base64urlEncode: (buf) => {
      return btoa(String.fromCharCode(...new Uint8Array(buf))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }
  };

  const saveTokens = (data) => {
    localStorage.setItem(STORAGE_KEYS.TOKEN, data.access_token);
    localStorage.setItem(STORAGE_KEYS.REFRESH, data.refresh_token);
    const exp = Date.now() + (data.expires_in - 10) * 1000;
    localStorage.setItem(STORAGE_KEYS.EXPIRES, exp);
  };

  const isLoggedIn = () => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    const exp = parseInt(localStorage.getItem(STORAGE_KEYS.EXPIRES) || '0', 10);
    return !!token && Date.now() < exp;
  };

  const login = async () => {
    const verifier = _crypto.generateRandomString();
    const state = _crypto.generateRandomString(16);
    const challenge = _crypto.base64urlEncode(await _crypto.sha256(verifier));
    localStorage.setItem(STORAGE_KEYS.CODE_VER, verifier);
    localStorage.setItem(STORAGE_KEYS.STATE, state);

    window.location.href = `${KEYCLOAK_BASE}/auth?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URI)}&scope=openid%20profile%20email&state=${state}&code_challenge=${challenge}&code_challenge_method=S256`;
  };

  const exchangeCode = async (code, state) => {
    if (state !== localStorage.getItem(STORAGE_KEYS.STATE)) throw new Error('CSRF Mismatch');

    const body = new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
      redirect_uri: REDIRECT_URI,
      code: code,
      code_verifier: localStorage.getItem(STORAGE_KEYS.CODE_VER),
    });

    const res = await fetch(`${KEYCLOAK_BASE}/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    });

    if (!res.ok) throw new Error('Falha na troca do token');
    saveTokens(await res.json());
    window.history.replaceState({}, document.title, REDIRECT_URI);
  };

  const getUser = () => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return { name: payload.name, email: payload.email, sub: payload.sub };
  };

  const logout = () => {
    Object.values(STORAGE_KEYS).forEach(k => localStorage.removeItem(k));
    window.location.href = `${KEYCLOAK_BASE}/logout?client_id=${CLIENT_ID}&post_logout_redirect_uri=${encodeURIComponent(REDIRECT_URI)}`;
  };

  return { init: async () => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');
    const state = params.get('state');
    if (code && state) { await exchangeCode(code, state); return true; }
    return isLoggedIn();
  }, login, logout, isLoggedIn, getUser };
})();