const Cart = {
  async load() {
    if (!Utils.requireAuth()) return;
    const container = document.getElementById('cartItems');
    const summary = document.getElementById('orderSummary');
    container.innerHTML = '<div class="page-loader"><div class="loader"></div></div>';

    try {
      const items = await API.cart.get();
      if (!items.length) {
        container.innerHTML = `
          <div class="empty-state glass">
            <h3>Your cart is empty</h3>
            <p>Discover our premium collection</p>
            <a href="products.html" class="btn btn-primary" style="margin-top:24px">Shop Now</a>
          </div>`;
        summary.innerHTML = '';
        return;
      }

      let subtotal = 0;
      container.innerHTML = items.map(item => {
        const lineTotal = item.price * item.quantity;
        subtotal += lineTotal;
        return `
          <div class="cart-item glass" data-id="${item.id}">
            <div class="cart-item-img"><img src="${item.imageUrl || 'https://via.placeholder.com/150x100?text=No+Image'}" alt="${item.name}" onerror="this.onerror=null;this.src='https://via.placeholder.com/150x100?text=No+Image'"></div>
            <div class="cart-item-info">
              <div class="cart-item-brand">${item.brand}</div>
              <div class="cart-item-name">${item.name}</div>
              <div>Size: ${item.size}</div>
              <div class="cart-item-price">${Utils.formatPrice(item.price)}</div>
            </div>
            <div class="cart-item-actions">
              <div class="qty-selector" style="margin:0">
                <button class="qty-btn" onclick="Cart.updateQty(${item.id}, ${item.quantity - 1})">−</button>
                <span class="qty-value">${item.quantity}</span>
                <button class="qty-btn" onclick="Cart.updateQty(${item.id}, ${item.quantity + 1})">+</button>
              </div>
              <button class="remove-btn" onclick="Cart.remove(${item.id})">Remove</button>
            </div>
          </div>`;
      }).join('');

      const shipping = subtotal > 5000 ? 0 : 199;
      const total = subtotal + shipping;

      summary.innerHTML = `
        <h3 style="font-family:var(--font-display);font-size:1.5rem;margin-bottom:24px">Order Summary</h3>
        <div class="summary-row"><span>Subtotal</span><span>${Utils.formatPrice(subtotal)}</span></div>
        <div class="summary-row"><span>Shipping</span><span>${shipping === 0 ? 'Free' : Utils.formatPrice(shipping)}</span></div>
        <div class="summary-row total"><span>Total</span><span>${Utils.formatPrice(total)}</span></div>
        <a id="checkoutBtn" href="#" class="btn btn-primary btn-block" style="margin-top:24px">Proceed to Checkout</a>
      `;
      // Attach click handler to checkout button to control navigation and logging
      const checkoutBtn = document.getElementById('checkoutBtn');
      if (checkoutBtn) {
        checkoutBtn.addEventListener('click', async (e) => {
          console.log('Checkout button clicked');
          e.preventDefault();
          try {
            const items = await API.cart.get();
            if (!items || !items.length) { window.location.href = 'cart.html'; return; }
            if (!localStorage.getItem('token')) { window.location.href = 'login.html?redirect=' + encodeURIComponent('checkout.html'); return; }
            window.location.href = 'checkout.html';
          } catch (err) {
            console.error('Checkout handler error', err);
            Utils.showToast(err.message || 'Unable to proceed to checkout', 'error');
          }
        });
      }
      updateCartCount();
    } catch (err) {
      container.innerHTML = `<div class="empty-state"><h3>Error</h3><p>${err.message}</p></div>`;
    }
  },

  async updateQty(cartId, qty) {
    if (qty < 1) return;
    try {
      console.log('Updating cart qty', { cartId, qty });
      await API.cart.update(cartId, qty);
      await Cart.load();
    } catch (err) { Utils.showToast(err.message, 'error'); }
  },

  async remove(cartId) {
    try {
      console.log('Removing cart item', cartId);
      await API.cart.remove(cartId);
      Utils.showToast('Item removed');
      await Cart.load();
    } catch (err) { Utils.showToast(err.message, 'error'); }
  }
};

const Checkout = {
  async init() {
    // Temporary client-only checkout for testing: open Razorpay popup immediately
    // No backend calls, no order creation, no cart/auth validation
    const form = document.getElementById('checkoutForm');
    const summary = document.getElementById('checkoutSummary');

    // Hardcoded test amount (in paise) and test key
    const TEST_AMOUNT_PAISE = 10000; // ₹100.00
    const RAZORPAY_TEST_KEY = 'rzp_test_1DP5mmOlF5G5ag';

    // Show a minimal summary so user sees the amount
    if (summary) {
      summary.innerHTML = `
        <h3 style="font-family:var(--font-display);font-size:1.5rem;margin-bottom:24px">Order Summary (Test)</h3>
        <div class="summary-row"><span>Test Item</span><span>₹100.00</span></div>
        <div class="summary-row total"><span>Total</span><span>₹100.00</span></div>
      `;
    }

    // Prefill from local storage if available
    try {
      const userEmail = localStorage.getItem('userEmail');
      const userName = localStorage.getItem('userName');
      if (form && form.shippingEmail) form.shippingEmail.value = userEmail || '';
      if (form && form.shippingName) form.shippingName.value = userName || '';
    } catch (err) { /* ignore */ }

    if (!form) return;

    form.addEventListener('submit', (e) => {
      e.preventDefault();

      const options = {
        key: RAZORPAY_TEST_KEY,
        amount: TEST_AMOUNT_PAISE,
        currency: 'INR',
        name: 'KRITHE STORE (Test)',
        description: 'Test Payment',
        handler: function(response) {
          console.log('Razorpay test success', response);
          try { Utils.showToast('Payment successful (test)', 'success'); } catch (err) { /* ignore */ }
        },
        prefill: {
          name: (form.shippingName && form.shippingName.value) || localStorage.getItem('userName') || '',
          email: (form.shippingEmail && form.shippingEmail.value) || localStorage.getItem('userEmail') || ''
        },
        theme: { color: '#c9a962' },
        modal: {
          ondismiss: () => {
            try { Utils.showToast('Payment popup closed', 'info'); } catch (err) { /* ignore */ }
          }
        }
      };

      try {
        const rzp = new Razorpay(options);
        rzp.open();
      } catch (err) {
        console.error('Failed to open Razorpay (test)', err);
        try { Utils.showToast('Unable to open payment popup', 'error'); } catch (e) { /* ignore */ }
      }
    });
  }
};

const Payment = {
  selectedMethod: 'upi',

  async init() {
    if (!Utils.requireAuth()) return;
    const order = JSON.parse(sessionStorage.getItem('currentOrder'));
    if (!order) { window.location.href = 'cart.html'; return; }

    document.getElementById('paymentAmount').textContent = Utils.formatPrice(order.totalAmount);
    document.getElementById('paymentOrderNum').textContent = order.orderNumber;

    document.querySelectorAll('.payment-method').forEach(el => {
      el.addEventListener('click', () => {
        document.querySelectorAll('.payment-method').forEach(m => m.classList.remove('active'));
        el.classList.add('active');
        Payment.selectedMethod = el.dataset.method;
      });
    });

    document.getElementById('payBtn').addEventListener('click', Payment.process);
  },

  async process() {
    const order = JSON.parse(sessionStorage.getItem('currentOrder'));
    const btn = document.getElementById('payBtn');
    btn.disabled = true;
    btn.textContent = 'Processing...';

    try {
      const razorpayData = await API.payments.createOrder(order.id);

      const options = {
        key: razorpayData.keyId || CONFIG.RAZORPAY_KEY,
        amount: razorpayData.amount * 100,
        currency: razorpayData.currency,
        name: 'KRITHE STORE',
        description: `Order ${razorpayData.orderNumber}`,
        order_id: razorpayData.razorpayOrderId,
        handler: async function(response) {
          try {
            const result = await API.payments.verify({
              orderId: order.id,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              method: Payment.selectedMethod
            });
            sessionStorage.setItem('completedOrder', JSON.stringify(result.order));
            window.location.href = 'order-success.html';
          } catch (err) {
            Utils.showToast(err.message, 'error');
            btn.disabled = false;
            btn.textContent = 'Pay Now';
          }
        },
        prefill: {
          name: localStorage.getItem('userName'),
          email: localStorage.getItem('userEmail')
        },
        theme: { color: '#c9a962' },
        modal: {
          ondismiss: () => {
            btn.disabled = false;
            btn.textContent = 'Pay Now';
          }
        }
      };

      const rzp = new Razorpay(options);
      rzp.open();
    } catch (err) {
      Utils.showToast(err.message, 'error');
      btn.disabled = false;
      btn.textContent = 'Pay Now';
    }
  }
};

const OrderSuccess = {
  init() {
    const order = JSON.parse(sessionStorage.getItem('completedOrder'));
    if (!order) { window.location.href = 'index.html'; return; }

    document.getElementById('orderNumber').textContent = order.orderNumber;
    document.getElementById('orderTotal').textContent = Utils.formatPrice(order.totalAmount);
    document.getElementById('orderDate').textContent = new Date(order.createdAt).toLocaleDateString('en-IN', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
    document.getElementById('orderStatus').textContent = order.status;

    sessionStorage.removeItem('currentOrder');
    sessionStorage.removeItem('completedOrder');
    updateCartCount();
  }
};
