const API = {
  async request(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    // Use AbortController to allow optional timeouts in future
    const controller = new AbortController();
    const signal = controller.signal;

    try {
      const res = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, { ...options, headers, signal });

      // Try to parse JSON safely
      let data;
      try {
        data = await res.json();
      } catch (e) {
        // Non-JSON response
        if (!res.ok) throw new Error(`Request failed: ${res.status} ${res.statusText}`);
        return null;
      }

      if (!res.ok || (data && data.success === false)) {
        throw new Error((data && data.message) ? data.message : `Request failed: ${res.status} ${res.statusText}`);
      }
      return data.data;
    } catch (err) {
      // Normalize fetch/network errors
      if (err.name === 'AbortError') throw new Error('Request timed out');
      if (err instanceof TypeError && err.message === 'Failed to fetch') {
        throw new Error('Network error: Unable to reach API. Is the backend running?');
      }
      throw err;
    }
  },

  auth: {
    register: (body) => API.request('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
    login: (body) => API.request('/auth/login', { method: 'POST', body: JSON.stringify(body) })
  },

  products: {
    getAll: (params = {}) => {
      const qs = new URLSearchParams(params).toString();
      return API.request(`/products?${qs}`);
    },
    getById: (id) => API.request(`/products/${id}`),
    getFeatured: () => API.request('/products/featured'),
    getTrending: () => API.request('/products/trending'),
    getRecent: () => API.request('/products/recent')
  },

  cart: {
    get: () => API.request('/cart'),
    add: (body) => API.request('/cart', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, quantity) => API.request(`/cart/${id}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
    remove: (id) => API.request(`/cart/${id}`, { method: 'DELETE' })
  },

  orders: {
    create: (body) => API.request('/orders', { method: 'POST', body: JSON.stringify(body) }),
    getAll: () => API.request('/orders'),
    getById: (id) => API.request(`/orders/${id}`)
  },

  payments: {
    createOrder: (orderId) => API.request('/payments/create-order', { method: 'POST', body: JSON.stringify({ orderId }) }),
    verify: (body) => API.request('/payments/verify', { method: 'POST', body: JSON.stringify(body) })
  },

  wishlist: {
    get: () => API.request('/wishlist'),
    add: (productId) => API.request(`/wishlist/${productId}`, { method: 'POST' }),
    remove: (productId) => API.request(`/wishlist/${productId}`, { method: 'DELETE' })
  }
};
