const API_BASE = 'http://localhost:5050/api/v1';

const Api = (() => {
  const _getHeaders = () => {
    const token = localStorage.getItem('myfinance_token'); 
    const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
  };

  async function request(method, path, body = null, params = {}) {
    const url = new URL(`${API_BASE}${path}`);
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
    });

    const opts = { method, headers: _getHeaders() };
    if (body) opts.body = JSON.stringify(body);

    try {
      const response = await fetch(url.toString(), opts);
      if (response.status === 401) {
        window.dispatchEvent(new CustomEvent('auth-expired'));
        return null;
      }
      if (response.status === 204) return { success: true };
      const data = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(data?.message || `Erro ${response.status}`);
      return data;
    } catch (err) {
      throw new Error(err.name === 'TypeError' ? 'Sem conexão com o servidor' : err.message);
    }
  }

  return Object.freeze({
    Users: {
      me: () => request('GET', '/users/me'),
      all: (page=0) => request('GET', '/users/allUsers', null, { page, size: 10 }),
      byEmail: (email) => request('GET', '/users/email', null, { email }),
      getUserById: (id) => request('GET', `/users/${id}`),
      delete: (id) => request('DELETE', `/users/${id}`)
    },
    Accounts: {
      all: (params = {}) => request('GET', '/accounts', null, params),
      byId: (id) => request('GET', `/accounts/${id}`),
      search: (name) => request('GET', '/accounts/search', null, { name }),
      create: (body) => request('POST', '/accounts', body),
      update: (id, body) => request('PATCH', `/accounts/${id}`, body),
      activate: (id) => request('PATCH', `/accounts/activate/${id}`),
      deactivate: (id) => request('PATCH', `/accounts/deactivate/${id}`),
      delete: (id) => request('DELETE', `/accounts/${id}`)
    },
    Categories: {
      all: (params = {}) => request('GET', '/categories', null, params),
      getById: (id) => request('GET', `/categories/${id}`),
      search: (name) => request('GET', '/categories/search', null, { name }),
      create: (body) => request('POST', '/categories', body),
      update: (id, body) => request('PATCH', `/categories/${id}`, body),
      activate: (id) => request('PATCH', `/categories/${id}/activate`),
      deactivate: (id) => request('PATCH', `/categories/${id}/deactivate`),
      delete: (id) => request('DELETE', `/categories/${id}`)
    },
    Transactions: {
      all: (params = {}) => request('GET', '/transactions/search', null, params),
      create: (body) => request('POST', '/transactions', body),
      transfer: (body) => request('POST', '/transactions/transfer', body),
      confirm: (id) => request('PATCH', `/transactions/${id}/confirm`),
      cancel: (id) => request('PATCH', `/transactions/${id}/cancel`),
      delete: (id) => request('DELETE', `/transactions/${id}`)
    },
    Goals: {
      all: (params = {}) => request('GET', '/goals', null, params),
      byId: (id) => request('GET', `/goals/${id}`),
      search: (name) => request('GET', '/goals/search', null, { name }),
      create: (body) => request('POST', '/goals', body),
      update: (id, body) => request('PATCH', `/goals/${id}`, body),
      deposit: (id, body) => request('POST', `/goals/${id}/deposit`, body), 
      withdraw: (id, body) => request('POST', `/goals/${id}/withdraw`, body),
      activate: (id) => request('PATCH', `/goals/${id}/activate`),
      deactivate: (id) => request('PATCH', `/goals/${id}/deactivate`),
      delete: (id) => request('DELETE', `/goals/${id}`)
    },
    Dashboard: {
      get: (startDate, endDate) => request('GET', '/dashboards', null, { startDate, endDate })
    }
  });
})();