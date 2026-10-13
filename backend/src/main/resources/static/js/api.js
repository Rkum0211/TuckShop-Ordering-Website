/* ================================================
   api.js — Tuck Shop API Service Layer
   All backend calls go here. Change API_BASE_URL
   to point at your Spring Boot server.
   ================================================ */

const API_BASE_URL = '/api';

const Api = (() => {
  // ── Helpers ──────────────────────────────────────────
  function getToken() { return sessionStorage.getItem('admin_token'); }

  async function request(method, path, body = null, auth = false) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
      const t = getToken();
      if (t) headers['Authorization'] = `Bearer ${t}`;
    }
    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(`${API_BASE_URL}${path}`, opts);

    if (res.status === 204) return null;

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const msg = data?.message || data?.error || `HTTP ${res.status}`;
      throw new Error(msg);
    }
    return data;
  }

  const get    = (path, auth) => request('GET',    path, null, auth);
  const post   = (path, body, auth) => request('POST',   path, body, auth);
  const put    = (path, body, auth) => request('PUT',    path, body, auth);
  const patch  = (path, body, auth) => request('PATCH',  path, body, auth);
  const del    = (path, auth) => request('DELETE',  path, null, auth);

  // ── Public APIs ───────────────────────────────────────
  const shop      = () => get('/shop-status');
  const products  = (params = '') => get(`/products${params}`);
  const product   = (id) => get(`/products/${id}`);
  const categories= () => get('/categories');
  const search    = (q) => get(`/products/search?q=${encodeURIComponent(q)}`);
  const qrCode    = () => get('/qr-code');

  const validateCoupon = (code, total) =>
    post('/coupons/validate', { code, orderTotal: total });

  const createOrder = (payload) => post('/orders', payload);
  const getOrder    = (token, phone) =>
    get(`/orders/${token}?phoneNumber=${encodeURIComponent(phone)}`);

  const createPayment = (payload) => post('/payments/create-order', payload);
  const verifyPayment = (payload) => post('/payments/verify', payload);

  // ── Admin Auth ────────────────────────────────────────
  const adminLogin  = (creds)  => post('/admin/auth/login', creds);
  const adminLogout = ()       => post('/admin/auth/logout', {}, true);

  // ── Admin Products ────────────────────────────────────
  const adminProducts     = (p = '') => get(`/admin/products${p}`, true);
  const adminAddProduct   = (b)      => post('/admin/products', b, true);
  const adminUpdateProduct= (id,b)   => put(`/admin/products/${id}`, b, true);
  const adminToggleAvail  = (id,b)   => patch(`/admin/products/${id}/availability`, b, true);
  const adminUpdateStock  = (id,b)   => patch(`/admin/products/${id}/stock`, b, true);
  const adminDeleteProduct= (id)     => del(`/admin/products/${id}`, true);

  // ── Admin Orders ──────────────────────────────────────
  const adminOrders       = (p = '') => get(`/admin/orders${p}`, true);
  const adminOrder        = (id)     => get(`/admin/orders/${id}`, true);
  const adminUpdateStatus = (id, b)  => patch(`/admin/orders/${id}/status`, b, true);
  const adminUpdatePaymentStatus = (id, b) => patch(`/admin/orders/${id}/payment-status`, b, true);
  const adminRefund       = (id)     => post(`/admin/orders/${id}/refund`, {}, true);

  // ── Admin Settings ────────────────────────────────────
  const adminGetSettings  = ()  => get('/admin/settings', true);
  const adminSaveSettings = (b) => put('/admin/settings', b, true);

  // ── Admin Coupons ─────────────────────────────────────
  const adminGetCoupons   = ()     => get('/admin/coupons', true);
  const adminAddCoupon    = (b)    => post('/admin/coupons', b, true);
  const adminUpdateCoupon = (id,b) => put(`/admin/coupons/${id}`, b, true);
  const adminDeleteCoupon = (id)   => del(`/admin/coupons/${id}`, true);

  // ── Admin Dashboard ───────────────────────────────────
  const adminDashboard = () => get('/admin/dashboard', true);
  const adminAuditLogs = (p = '') => get(`/admin/audit-logs${p}`, true);

  return {
    getToken, shop, products, product, categories, search,
    qrCode, validateCoupon, createOrder, getOrder,
    createPayment, verifyPayment,
    adminLogin, adminLogout,
    adminProducts, adminAddProduct, adminUpdateProduct,
    adminToggleAvail, adminUpdateStock, adminDeleteProduct,
    adminOrders, adminOrder, adminUpdateStatus, adminUpdatePaymentStatus, adminRefund,
    adminGetSettings, adminSaveSettings,
    adminGetCoupons, adminAddCoupon, adminUpdateCoupon, adminDeleteCoupon,
    adminDashboard, adminAuditLogs,
  };
})();
