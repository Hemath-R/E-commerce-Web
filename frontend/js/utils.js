const Utils = {
  // When true, error toasts will be suppressed and printed to console instead.
  SILENCE_ERROR_TOASTS: true,

  formatPrice(amount) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  },

  getEffectivePrice(product) {
    return product.discountPrice || product.price;
  },

  renderStars(rating) {
    const full = Math.floor(rating);
    const half = rating % 1 >= 0.5;
    let stars = '';
    for (let i = 0; i < full; i++) stars += '★';
    if (half) stars += '½';
    while (stars.length < 5) stars += '☆';
    return stars.substring(0, 5);
  },

  showToast(message, type = 'success') {
    if (type === 'error' && Utils.SILENCE_ERROR_TOASTS) {
      console.warn('Suppressed error toast:', message);
      return;
    }
    let toast = document.querySelector('.toast');
    if (!toast) {
      toast = document.createElement('div');
      toast.className = 'toast';
      document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => toast.classList.remove('show'), 3000);
  },

  hideLoader() {
    const loader = document.querySelector('.loader-overlay');
    if (loader) loader.classList.add('hidden');
  },

  getQueryParam(key) {
    return new URLSearchParams(window.location.search).get(key);
  },

  requireAuth(redirect = 'login.html') {
    if (!localStorage.getItem('token')) {
      window.location.href = redirect + '?redirect=' + encodeURIComponent(window.location.pathname);
      return false;
    }
    return true;
  },

  validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  },

  debounce(fn, delay = 300) {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn(...args), delay);
    };
  }
};

document.addEventListener('DOMContentLoaded', () => {
  setTimeout(Utils.hideLoader, 600);
});
