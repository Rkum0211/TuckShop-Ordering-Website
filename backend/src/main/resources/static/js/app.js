/* ================================================
   app.js — Shared utilities & helpers
   ================================================ */

// ── Toast notifications ────────────────────────
function showToast(msg, type = 'success', dur = 3500) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container-custom';
    document.body.appendChild(container);
  }
  const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast-custom ${type}`;
  toast.innerHTML = `<span class="toast-icon">${icons[type] || icons.info}</span><span>${msg}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(120%)';
    toast.style.transition = 'all 0.4s ease';
    setTimeout(() => toast.remove(), 400);
  }, dur);
}

// ── Format currency ────────────────────────────
function formatINR(amount) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', minimumFractionDigits: 0 }).format(amount);
}

// ── Scroll reveal ──────────────────────────────
function initScrollReveal() {
  const els = document.querySelectorAll('.scroll-reveal');
  if (!els.length) return;
  const observer = new IntersectionObserver(
    entries => entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('revealed'); observer.unobserve(e.target); } }),
    { threshold: 0.12 }
  );
  els.forEach(el => observer.observe(el));
}

// ── Shop status banner ─────────────────────────
async function loadShopStatus() {
  const badges = document.querySelectorAll('.shop-status-badge');
  if (!badges.length) return;
  try {
    const s = await Api.shop();
    const open = s.isOpen;
    badges.forEach(b => {
      b.className = `shop-status-badge ${open ? 'open' : 'closed'}`;
      b.innerHTML = `<span class="status-dot"></span>${open ? '🟢 Open Now' : '🔴 Closed'}`;
    });
    document.querySelectorAll('.shop-timing-text').forEach(el => {
      el.textContent = `${s.openingTime} – ${s.closingTime}`;
    });
    document.querySelectorAll('.order-now-btn').forEach(btn => {
      if (!open) { btn.classList.add('disabled'); btn.title = 'Shop is currently closed'; }
    });
  } catch {
    badges.forEach(b => { b.className = 'shop-status-badge open'; b.innerHTML = '<span class="status-dot"></span>🟢 Open Now'; });
  }
}

// ── Scroll-to-top button ───────────────────────
function initScrollTop() {
  const btn = document.getElementById('scrollTopBtn');
  if (!btn) return;
  window.addEventListener('scroll', () => {
    btn.style.opacity = window.scrollY > 300 ? '1' : '0';
    btn.style.pointerEvents = window.scrollY > 300 ? 'all' : 'none';
  });
  btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
}

// ── Indian mobile number validation ───────────
function isValidMobile(num) { return /^[6-9]\d{9}$/.test(num.replace(/\s|-/g, '')); }

// ── Razorpay Checkout helper ───────────────────
async function openRazorpay({ orderId, amount, keyId, customerName, phone, description, onSuccess, onFailure }) {
  return new Promise((resolve, reject) => {
    // If running in demo mode or Razorpay JS SDK is not loaded
    const isDemo = !keyId || keyId.includes('mock') || keyId.includes('demo') || typeof window.Razorpay === 'undefined';

    if (isDemo) {
      setTimeout(() => {
        const mockResponse = {
          razorpay_order_id: orderId || `order_demo_${Date.now()}`,
          razorpay_payment_id: `pay_demo_${Date.now()}`,
          razorpay_signature: `sig_demo_${Date.now()}`
        };
        onSuccess && onSuccess(mockResponse);
        resolve(mockResponse);
      }, 700);
      return;
    }

    const options = {
      key:          keyId,
      amount:       amount,          // in paise
      currency:     'INR',
      name:         'My Tuck Shop',
      description:  description || 'Order Payment',
      order_id:     orderId,
      prefill:      { name: customerName, contact: phone },
      theme:        { color: '#FF6B35' },
      modal:        { ondismiss: () => { onFailure && onFailure('dismissed'); reject(new Error('Payment dismissed')); } },
      handler: (response) => { onSuccess && onSuccess(response); resolve(response); }
    };
    try {
      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (e) {
      // If popup fails to open, fallback to simulated success for smooth dev testing
      const mockResponse = {
        razorpay_order_id: orderId || `order_demo_${Date.now()}`,
        razorpay_payment_id: `pay_demo_${Date.now()}`,
        razorpay_signature: `sig_demo_${Date.now()}`
      };
      onSuccess && onSuccess(mockResponse);
      resolve(mockResponse);
    }
  });
}

// ── DOMContentLoaded init ──────────────────────
document.addEventListener('DOMContentLoaded', () => {
  initScrollReveal();
  loadShopStatus();
  initScrollTop();
});
