/* ================================================
   admin.js — Shared admin utilities
   ================================================ */

// ── Auth Guard ─────────────────────────────────
function requireAuth() {
  const token = sessionStorage.getItem('admin_token');
  if (!token) { window.location.href = 'login.html'; return; }
}

// ── Logout ─────────────────────────────────────
async function logout() {
  try { await Api.adminLogout(); } catch {}
  sessionStorage.removeItem('admin_token');
  sessionStorage.removeItem('admin_user');
  window.location.href = 'login.html';
}

// ── Admin Toast ────────────────────────────────
function adminToast(msg, type = 'success', dur = 3500) {
  let container = document.getElementById('adminToastContainer');
  if (!container) return;
  const icons = { success:'✅', error:'❌', warning:'⚠️', info:'ℹ️' };
  const el = document.createElement('div');
  el.className = `admin-toast ${type}`;
  el.innerHTML = `<span class="toast-icon">${icons[type]||icons.info}</span><span class="admin-toast-msg">${msg}</span><button class="admin-toast-close" onclick="this.parentElement.remove()">×</button>`;
  container.appendChild(el);
  setTimeout(() => { el.style.opacity='0'; el.style.transform='translateX(120%)'; el.style.transition='all .3s'; setTimeout(()=>el.remove(),300); }, dur);
}

// ── Sidebar toggle ─────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const toggleBtn = document.getElementById('sidebarToggle');
  const sidebar   = document.getElementById('adminSidebar');
  const overlay   = document.getElementById('sidebarOverlay');
  if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
      overlay.classList.toggle('show');
    });
  }
});
function closeSidebar() {
  document.getElementById('adminSidebar')?.classList.remove('open');
  document.getElementById('sidebarOverlay')?.classList.remove('show');
}

// ── Shop toggle ────────────────────────────────
let shopIsOpen = true;
async function toggleShop() {
  shopIsOpen = !shopIsOpen;
  const label = document.getElementById('shopStatusLabel');
  const dot   = document.getElementById('shopDot');
  const icon  = document.getElementById('shopToggleIcon');
  if (label) label.textContent = shopIsOpen ? 'Open' : 'Closed';
  if (dot)   dot.style.background = shopIsOpen ? '#10B981' : '#EF4444';
  if (icon)  { icon.className = shopIsOpen ? 'fas fa-toggle-on' : 'fas fa-toggle-off'; icon.style.color = shopIsOpen ? '#10B981':'#EF4444'; }
  adminToast(`Shop is now ${shopIsOpen?'OPEN':'CLOSED'}`, shopIsOpen?'success':'warning');
}

// ── Currency ───────────────────────────────────
function fINR(n) { return `₹${Number(n||0).toLocaleString('en-IN')}`; }

// ── Date format ────────────────────────────────
function fDate(d) { return d ? new Date(d).toLocaleString('en-IN',{day:'2-digit',month:'short',hour:'2-digit',minute:'2-digit'}) : '—'; }

// ── Status pills ───────────────────────────────
const ORDER_STATUS_PILL  = { PENDING:'pill-pending', CONFIRMED:'pill-confirmed', PREPARING:'pill-preparing', READY_FOR_PICKUP:'pill-ready', COMPLETED:'pill-completed', CANCELLED:'pill-cancelled' };
const PAYMENT_STATUS_PILL= { PAID:'pill-paid', PENDING:'pill-pending', FAILED:'pill-failed', REFUNDED:'pill-refunded' };
function orderPill(s)   { return `<span class="status-pill ${ORDER_STATUS_PILL[s]||'pill-pending'}">${(s||'').replace(/_/g,' ')}</span>`; }
function paymentPill(s) { return `<span class="status-pill ${PAYMENT_STATUS_PILL[s]||'pill-pending'}">${s||'PENDING'}</span>`; }
