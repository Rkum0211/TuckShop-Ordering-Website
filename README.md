# 🛒 Tuck Shop Ordering Website

A full-stack web application designed for campus tuck shops, cafeterias, and quick-service food counters. Students and customers can browse menus, customize cart items, apply discount coupons, make payments, and track live pickup tokens. Store administrators can manage inventory, fulfill incoming orders, adjust operating hours, and view business analytics.

---

## 🌟 Key Features

### Customer Experience
- 🍔 **Interactive Menu:** Browse categories (Snacks, Drinks, Bakery, Chocolates, Stationery) with real-time stock indicators.
- 🛒 **Smart Cart:** Dynamic quantity adjustments and instant price calculation.
- 🎟️ **Promo Coupons:** Built-in discount validation (e.g., `WELCOME10`, `SNACK20`, `FLAT50`).
- 💳 **Seamless Payments:** Integrated Razorpay checkout flow (with simulated sandbox demo mode).
- 🏷️ **Live Order Tracking:** Token-based pickup status page with QR code.

### Admin & Staff Management
- 📊 **Executive Dashboard:** Live stats on today's revenue, order counts, and active products.
- 📦 **Order Queue:** Real-time state transitions (`RECEIVED` ➔ `PREPARING` ➔ `READY` ➔ `COLLECTED`).
- 🏷️ **Product Management:** Add/edit products, update pricing, and toggle in-stock availability.
- 🎟️ **Coupon Manager:** Create and manage percentage or flat-discount promo codes.
- ⚙️ **Shop Configuration:** Control store open/close state, daily order limits, and counter details.
- 📜 **Audit Logs:** Full administrative activity tracking for security.

---

## 🛠️ Technology Stack

- **Backend:** Java 21, Spring Boot 3.3 (Spring MVC, Spring Data JPA, Spring Security)
- **Database:** H2 In-Memory Database (default for instant setup) / MySQL 8 support via Docker
- **Security:** JWT (JSON Web Tokens) & BCrypt password encryption
- **Frontend:** Responsive HTML5, Vanilla JavaScript, CSS3, Bootstrap 5, FontAwesome
- **API Docs:** SpringDoc OpenAPI 3 / Swagger UI

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** or later
- **Maven 3.8+**

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Rkum0211/TuckShop-Ordering-Website.git
   cd TuckShop-Ordering-Website/backend
   ```

2. **Start the backend server:**
   ```bash
   mvn spring-boot:run
   ```

3. **Open the application in your browser:**
   - **Storefront:** [http://localhost:8080](http://localhost:8080)
   - **Menu:** [http://localhost:8080/menu.html](http://localhost:8080/menu.html)
   - **Admin Portal:** [http://localhost:8080/admin/login.html](http://localhost:8080/admin/login.html)
   - **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔑 Default Credentials & Coupons

- **Admin Username:** `admin`
- **Admin Password:** `admin123`
- **Sample Coupons:**
  - `WELCOME10` — 10% discount (min order ₹40)
  - `SNACK20` — 20% discount (min order ₹100)
  - `FLAT50` — ₹50 flat discount (min order ₹200)

---

## 🐳 Running with Docker Compose (Optional MySQL)

```bash
cd backend
docker-compose up -d
```
