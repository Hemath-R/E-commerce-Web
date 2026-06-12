const Auth = {
  saveSession(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userName', data.fullName);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userRole', data.role);
  },

  logout() {
    ['token', 'userId', 'userName', 'userEmail', 'userRole'].forEach(k => localStorage.removeItem(k));
    window.location.href = 'index.html';
  },

  isLoggedIn() {
    return !!localStorage.getItem('token');
  },

  initLoginForm() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = form.email.value.trim();
      const password = form.password.value;
      let valid = true;

      document.querySelectorAll('.form-error').forEach(el => el.classList.remove('show'));

      if (!Utils.validateEmail(email)) {
        form.querySelector('[data-error="email"]').classList.add('show');
        valid = false;
      }
      if (password.length < 6) {
        form.querySelector('[data-error="password"]').classList.add('show');
        valid = false;
      }
      if (!valid) return;

      try {
        const data = await API.auth.login({ email, password });
        Auth.saveSession(data);
        Utils.showToast('Welcome back!');
        const redirect = Utils.getQueryParam('redirect') || 'index.html';
        setTimeout(() => window.location.href = redirect, 500);
      } catch (err) {
        Utils.showToast(err.message, 'error');
      }
    });
  },

  initRegisterForm() {
    const form = document.getElementById('registerForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fullName = form.fullName.value.trim();
      const email = form.email.value.trim();
      const password = form.password.value;
      const confirmPassword = form.confirmPassword.value;
      const phone = form.phone?.value.trim() || '';
      let valid = true;

      document.querySelectorAll('.form-error').forEach(el => el.classList.remove('show'));

      if (fullName.length < 2) { form.querySelector('[data-error="fullName"]').classList.add('show'); valid = false; }
      if (!Utils.validateEmail(email)) { form.querySelector('[data-error="email"]').classList.add('show'); valid = false; }
      if (password.length < 6) { form.querySelector('[data-error="password"]').classList.add('show'); valid = false; }
      if (password !== confirmPassword) { form.querySelector('[data-error="confirmPassword"]').classList.add('show'); valid = false; }
      if (!valid) return;

      try {
        const data = await API.auth.register({ fullName, email, password, phone });
        Auth.saveSession(data);
        Utils.showToast('Account created successfully!');
        setTimeout(() => window.location.href = 'index.html', 500);
      } catch (err) {
        Utils.showToast(err.message, 'error');
      }
    });
  }
};
