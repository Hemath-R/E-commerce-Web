function renderNavbar() {
  const nav = document.getElementById('navbar');
  if (!nav) return;

  const isLoggedIn = Auth.isLoggedIn();
  const currentPage = window.location.pathname.split('/').pop() || 'index.html';

  nav.innerHTML = `
    <div class="container nav-inner">
      <a href="index.html" class="logo">KRITHE <span>STORE</span></a>
      <nav class="nav-links" id="navLinks">
        <a href="index.html" class="${currentPage === 'index.html' ? 'active' : ''}">Home</a>
        <a href="products.html" class="${currentPage === 'products.html' ? 'active' : ''}">Shop</a>
        ${isLoggedIn ? `<a href="cart.html" class="${currentPage === 'cart.html' ? 'active' : ''}">Cart</a>` : ''}
      </nav>
      <div class="nav-actions">
        ${isLoggedIn ? `
          <a href="cart.html" class="cart-badge btn-ghost" aria-label="Cart">
            <svg width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24"><path d="M6 6h15l-1.5 9H7.5L6 6zM6 6L5 3H2M9 20a1 1 0 100-2 1 1 0 000 2zm8 0a1 1 0 100-2 1 1 0 000 2z"/></svg>
            <span class="cart-count" id="navCartCount">0</span>
          </a>
          <button class="btn btn-ghost btn-sm" onclick="Auth.logout()">Logout</button>
        ` : `
          <a href="login.html" class="btn btn-ghost btn-sm">Login</a>
          <a href="register.html" class="btn btn-primary btn-sm">Sign Up</a>
        `}
        <button class="hamburger" id="hamburger" aria-label="Menu">
          <span></span><span></span><span></span>
        </button>
      </div>
    </div>
  `;

    // Navbar builder + link normalization and simple broken-route logging
    // Normalize links that accidentally contain filesystem paths from developer mistakes
    function normalizeHref(href) {
      if (!href) return href;
      // common dev-path fragments to strip
      const fragments = [
        'backend/src/main/resources/static/',
        '/src/main/resources/static/',
        'src/main/resources/static/'
      ];
      for (const f of fragments) {
        const idx = href.indexOf(f);
        if (idx !== -1) {
          // take everything after the fragment
          return href.substring(idx + f.length);
        }
      }
      return href;
    }

    // Intercept anchor clicks to normalize and optionally check route before navigating
    document.addEventListener('click', async function (e) {
      const a = e.target.closest && e.target.closest('a');
      if (!a) return;
      const raw = a.getAttribute('href');
      if (!raw) return;

      const normalized = normalizeHref(raw);
      if (normalized !== raw) {
        e.preventDefault();
        console.info('Normalized navbar link:', raw, '→', normalized);
        // quick HEAD check to warn about missing pages (best-effort)
        try {
          const resp = await fetch(normalized, { method: 'HEAD' });
          if (!resp.ok) {
            console.warn('Route may be missing (HEAD):', normalized, resp.status);
          }
        } catch (err) {
          console.warn('Route HEAD check failed for', normalized, err);
        }
        window.location.href = normalized;
        return;
      }

      // For same-origin links, do a lightweight HEAD check and log 404s
      try {
        const url = new URL(raw, window.location.href);
        if (url.origin === window.location.origin) {
          const resp = await fetch(url.toString(), { method: 'HEAD' });
          if (!resp.ok) {
            console.warn('Navigation target returned non-OK status:', url.toString(), resp.status);
          }
        }
      } catch (err) {
        // ignore URL parse errors
      }
    }, true);

  document.getElementById('hamburger')?.addEventListener('click', () => {
    document.getElementById('navLinks').classList.toggle('open');
  });

  window.addEventListener('scroll', () => {
    nav.classList.toggle('scrolled', window.scrollY > 50);
  });

  if (isLoggedIn) updateCartCount();
}

async function updateCartCount() {
  try {
    const items = await API.cart.get();
    const count = items.reduce((sum, i) => sum + i.quantity, 0);
    console.log('updateCartCount ->', count);
    const el = document.getElementById('navCartCount');
    if (el) el.textContent = count;
  } catch {}
}

function renderFooter() {
  const footer = document.getElementById('footer');
  if (!footer) return;

  footer.innerHTML = `
    <div class="container">
      <div class="footer-grid">
        <div class="footer-brand">
          <a href="index.html" class="logo">KRITHE <span>STORE</span></a>
          <p>Premium footwear curated for those who walk with purpose. Luxury meets comfort in every step.</p>
        </div>
        <div>
          <h4>Shop</h4>
          <ul class="footer-links">
            <li><a href="products.html">All Products</a></li>
            <li><a href="products.html?category=Sneakers">Sneakers</a></li>
            <li><a href="products.html?category=Running">Running</a></li>
            <li><a href="products.html?category=Formal">Formal</a></li>
          </ul>
        </div>
        <div>
          <h4>Account</h4>
          <ul class="footer-links">
            <li><a href="login.html">Login</a></li>
            <li><a href="register.html">Register</a></li>
            <li><a href="cart.html">Cart</a></li>
          </ul>
        </div>
        <div>
          <h4>Contact</h4>
          <ul class="footer-links">
            <li><a href="mailto:hello@krithestore.com">hello@krithestore.com</a></li>
            <li><a href="tel:+911234567890">+91 123 456 7890</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <span>&copy; 2024 KRITHE STORE. All rights reserved.</span>
        <span>Crafted with precision</span>
      </div>
    </div>
  `;
}

document.addEventListener('DOMContentLoaded', () => {
  renderNavbar();
  renderFooter();
});
