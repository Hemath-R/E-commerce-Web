const Products = {
  renderCard(product) {
    const price = Utils.getEffectivePrice(product);
    const hasDiscount = product.discountPrice && product.discountPrice < product.price;
    return `
      <article class="product-card fade-in" onclick="window.location.href='product-details.html?id=${product.id}'">
        <div class="product-card-img">
          <img src="${product.imageUrl || 'https://via.placeholder.com/600x400?text=No+Image'}" alt="${product.name}" loading="lazy" onerror="this.onerror=null;this.src='https://via.placeholder.com/600x400?text=No+Image'">
          ${product.trending ? '<span class="product-badge">Trending</span>' : ''}
          ${product.featured && !product.trending ? '<span class="product-badge">Featured</span>' : ''}
          <button class="product-wishlist" onclick="event.stopPropagation(); Products.toggleWishlist(${product.id}, this)" aria-label="Wishlist">
            <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
          </button>
        </div>
        <div class="product-card-body">
          <div class="product-brand">${product.brand || ''}</div>
          <h3 class="product-name">${product.name}</h3>
          <div class="product-rating">
            <span class="stars">${Utils.renderStars(product.rating)}</span>
            <span>(${product.reviewCount})</span>
          </div>
          <div class="product-price">
            <span class="price-current">${Utils.formatPrice(price)}</span>
            ${hasDiscount ? `<span class="price-original">${Utils.formatPrice(product.price)}</span>` : ''}
          </div>
        </div>
      </article>
    `;
  },

  async toggleWishlist(productId, btn) {
    if (!Auth.isLoggedIn()) {
      window.location.href = 'login.html';
      return;
    }
    try {
      await API.wishlist.add(productId);
      btn.style.color = 'var(--error)';
      Utils.showToast('Added to wishlist');
    } catch (err) {
      Utils.showToast(err.message, 'error');
    }
  },

  async loadGrid(containerId, params = {}) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '<div class="page-loader"><div class="loader"></div></div>';

    try {
      const data = await API.products.getAll(params);
      const products = data.content || data;
      if (!products.length) {
        container.innerHTML = '<div class="empty-state"><h3>No products found</h3><p>Try adjusting your filters</p></div>';
        return;
      }
      container.innerHTML = products.map(p => Products.renderCard(p)).join('');
      if (data.totalPages > 1) Products.renderPagination(data);
    } catch (err) {
      container.innerHTML = `<div class="empty-state"><h3>Error loading products</h3><p>${err.message}</p></div>`;
    }
  },

  renderPagination(data) {
    const el = document.getElementById('pagination');
    if (!el) return;
    let html = '';
    for (let i = 0; i < data.totalPages; i++) {
      html += `<button class="page-btn ${i === data.number ? 'active' : ''}" onclick="Products.goToPage(${i})">${i + 1}</button>`;
    }
    el.innerHTML = html;
  },

  goToPage(page) {
    const params = new URLSearchParams(window.location.search);
    params.set('page', page);
    window.location.search = params.toString();
  },

  initProductsPage() {
    const searchInput = document.getElementById('searchInput');
    const categoryFilter = document.getElementById('categoryFilter');
    const sortFilter = document.getElementById('sortFilter');

    const load = () => {
      const params = {
        search: searchInput?.value || '',
        category: categoryFilter?.value || Utils.getQueryParam('category') || '',
        sort: sortFilter?.value || 'default',
        page: Utils.getQueryParam('page') || 0,
        size: CONFIG.ITEMS_PER_PAGE
      };
      Products.loadGrid('productGrid', params);
    };

    if (categoryFilter && Utils.getQueryParam('category')) {
      categoryFilter.value = Utils.getQueryParam('category');
    }

    searchInput?.addEventListener('input', Utils.debounce(load, 400));
    categoryFilter?.addEventListener('change', load);
    sortFilter?.addEventListener('change', load);
    load();
  },

  async initProductDetail() {
    const id = Utils.getQueryParam('id');
    if (!id) { window.location.href = 'products.html'; return; }

    try {
      const data = await API.products.getById(id);
      const product = data.product;
      const related = data.relatedProducts || [];

      document.title = `${product.name} — KRITHE STORE`;
      document.getElementById('detailBrand').textContent = product.brand;
      document.getElementById('detailTitle').textContent = product.name;
      document.getElementById('detailRating').innerHTML = `
        <span class="stars">${Utils.renderStars(product.rating)}</span>
        <span>${product.rating} (${product.reviewCount} reviews)</span>
      `;
      const price = Utils.getEffectivePrice(product);
      document.getElementById('detailPrice').innerHTML = `
        <span class="price-current">${Utils.formatPrice(price)}</span>
        ${product.discountPrice ? `<span class="price-original">${Utils.formatPrice(product.price)}</span>` : ''}
      `;
      document.getElementById('detailDesc').textContent = product.description;
      const mainImage = document.getElementById('mainImage');
      mainImage.src = product.imageUrl || 'https://via.placeholder.com/600x400?text=No+Image';
      mainImage.alt = product.name;
      mainImage.onerror = () => { console.warn('Main image failed to load, using placeholder'); mainImage.src = 'https://via.placeholder.com/600x400?text=No+Image'; };

      const thumbs = document.getElementById('galleryThumbs');
      const images = [product.imageUrl || 'https://via.placeholder.com/150x100?text=No+Image'];
      thumbs.innerHTML = images.map((img, i) => `
        <div class="gallery-thumb ${i === 0 ? 'active' : ''}" onclick="document.getElementById('mainImage').src='${img}'; document.querySelectorAll('.gallery-thumb').forEach(t=>t.classList.remove('active')); this.classList.add('active')">
          <img src="${img}" alt="" onerror="this.onerror=null;this.src='https://via.placeholder.com/150x100?text=No+Image'">
        </div>
      `).join('');

      let qty = 1;
      let selectedSize = '9';
      const qtyEl = document.getElementById('qtyValue');
      document.getElementById('qtyMinus').onclick = () => { if (qty > 1) { qty--; qtyEl.textContent = qty; } };
      document.getElementById('qtyPlus').onclick = () => { if (qty < product.stock) { qty++; qtyEl.textContent = qty; } };

      document.querySelectorAll('.size-btn').forEach(btn => {
        btn.onclick = () => {
          document.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
          selectedSize = btn.dataset.size;
        };
      });

      const addBtn = document.getElementById('addToCartBtn');
      if (addBtn) {
        addBtn.addEventListener('click', async () => {
          console.log('Add to cart clicked', { productId: product.id, qty, selectedSize });
          if (!Auth.isLoggedIn()) { window.location.href = 'login.html'; return; }
          addBtn.disabled = true;
          try {
            const res = await API.cart.add({ productId: product.id, quantity: qty, size: selectedSize });
            console.log('API.cart.add response', res);
            Utils.showToast('Added to cart!');
            updateCartCount();
            // After adding to cart, automatically open checkout
            console.log('Redirecting to checkout.html');
            window.location.href = 'checkout.html';
          } catch (err) {
            console.error('Add to cart error', err);
            Utils.showToast(err.message, 'error');
          } finally {
            addBtn.disabled = false;
          }
        });
      }

      const buyNowBtn = document.getElementById('buyNowBtn');
      if (buyNowBtn) {
        buyNowBtn.addEventListener('click', async () => {
          console.log('Buy now clicked');
          if (!Auth.isLoggedIn()) { window.location.href = 'login.html'; return; }
          buyNowBtn.disabled = true;
          try {
            await API.cart.add({ productId: product.id, quantity: qty, size: selectedSize });
            window.location.href = 'checkout.html';
          } catch (err) {
            console.error('Buy now error', err);
            Utils.showToast(err.message, 'error');
          } finally {
            buyNowBtn.disabled = false;
          }
        });
      }

      const relatedEl = document.getElementById('relatedProducts');
      if (relatedEl && related.length) {
        relatedEl.innerHTML = related.map(p => Products.renderCard(p)).join('');
      }
    } catch (err) {
      Utils.showToast(err.message, 'error');
    }
  }
};
