/* ================================================
   cart.js — Shopping Cart (localStorage-backed)
   ================================================ */

const Cart = (() => {
  const STORAGE_KEY = 'tuck_cart';

  function load()        { try { return JSON.parse(localStorage.getItem(STORAGE_KEY)) || []; } catch { return []; } }
  function save(items)   { localStorage.setItem(STORAGE_KEY, JSON.stringify(items)); updateBadge(); }
  function getItems()    { return load(); }
  function clear()       { localStorage.removeItem(STORAGE_KEY); updateBadge(); }

  function addItem(product, qty = 1) {
    const items = load();
    const idx   = items.findIndex(i => i.productId === product.id);
    if (idx > -1) {
      const newQty = items[idx].quantity + qty;
      items[idx].quantity = Math.min(newQty, product.maxQuantityPerOrder || 10);
    } else {
      items.push({
        productId:           product.id,
        productName:         product.name,
        price:               product.price,
        discountedPrice:     product.discountedPrice || product.price,
        imageUrl:            product.imageUrl || '',
        quantity:            qty,
        maxQuantityPerOrder: product.maxQuantityPerOrder || 10,
      });
    }
    save(items);
    return items;
  }

  function updateQty(productId, qty) {
    const items = load();
    const idx   = items.findIndex(i => i.productId === productId);
    if (idx > -1) {
      if (qty <= 0) {
        items.splice(idx, 1);
      } else {
        items[idx].quantity = Math.min(qty, items[idx].maxQuantityPerOrder);
      }
    }
    save(items);
    return items;
  }

  function removeItem(productId) {
    const items = load().filter(i => i.productId !== productId);
    save(items);
    return items;
  }

  function getCount() {
    return load().reduce((s, i) => s + i.quantity, 0);
  }

  function getSubtotal() {
    return load().reduce((s, i) => s + (i.discountedPrice * i.quantity), 0);
  }

  function updateBadge() {
    const cnt = getCount();
    document.querySelectorAll('.cart-count').forEach(el => {
      el.textContent = cnt;
      el.style.display = cnt > 0 ? 'flex' : 'none';
    });
    document.querySelectorAll('.cart-count-text').forEach(el => {
      el.textContent = cnt > 0 ? `(${cnt})` : '';
    });
  }

  // Call on page load
  document.addEventListener('DOMContentLoaded', updateBadge);

  return { getItems, addItem, updateQty, removeItem, getCount, getSubtotal, clear, updateBadge };
})();
