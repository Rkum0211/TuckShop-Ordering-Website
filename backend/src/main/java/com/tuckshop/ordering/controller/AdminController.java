package com.tuckshop.ordering.controller;

import com.tuckshop.ordering.entity.*;
import com.tuckshop.ordering.service.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final SettingsService settingsService;
    private final AuditService auditService;

    public AdminController(ProductService productService, OrderService orderService, CouponService couponService, SettingsService settingsService, AuditService auditService) {
        this.productService = productService;
        this.orderService = orderService;
        this.couponService = couponService;
        this.settingsService = settingsService;
        this.auditService = auditService;
    }

    private void logAction(String action, String entityType, String entityId, String description) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.logAction(username != null ? username : "admin", action, entityType, entityId, description, "127.0.0.1");
    }

    // --- Dashboard ---
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> data = orderService.getDashboardStats();
        List<Product> lowStock = productService.getLowStockProducts();
        data.put("lowStockProducts", lowStock);
        data.put("lowStockCount", lowStock != null ? lowStock.size() : 0);
        return ResponseEntity.ok(data);
    }

    // --- Products ---
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, category, available, search));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productService.createProduct(product);
        logAction("PRODUCT_CREATE", "Product", saved.getId().toString(), "Created product: " + saved.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        logAction("PRODUCT_UPDATE", "Product", id.toString(), "Updated product: " + updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        logAction("PRODUCT_DELETE", "Product", id.toString(), "Deleted product ID: " + id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/products/{id}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Product p = productService.getProductById(id);
        if (payload.containsKey("available")) {
            p.setAvailable(payload.get("available"));
            productService.updateProduct(id, p);
            logAction("PRODUCT_UPDATE", "Product", id.toString(), "Toggled availability to " + payload.get("available"));
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<Product> updateProductStock(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Integer stock = payload.get("stock") != null ? Integer.valueOf(payload.get("stock").toString())
                      : payload.get("stockQuantity") != null ? Integer.valueOf(payload.get("stockQuantity").toString()) : 0;
        Product updated = productService.updateStock(id, stock);
        logAction("PRODUCT_UPDATE", "Product", id.toString(), "Updated stock quantity to " + stock);
        return ResponseEntity.ok(updated);
    }

    // --- Orders ---
    @GetMapping("/orders")
    public ResponseEntity<Page<CustomerOrder>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, status, paymentStatus, search));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<CustomerOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<CustomerOrder> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        OrderStatus newStatus = OrderStatus.valueOf(payload.get("orderStatus"));
        CustomerOrder updated = orderService.updateOrderStatus(id, newStatus);
        logAction("ORDER_STATUS_UPDATE", "Order", id.toString(), "Status updated to " + newStatus);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/orders/{id}/payment-status")
    public ResponseEntity<CustomerOrder> updatePaymentStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        PaymentStatus newStatus = PaymentStatus.valueOf(payload.get("paymentStatus"));
        CustomerOrder updated = orderService.updatePaymentStatus(id, newStatus);
        logAction("PAYMENT_STATUS_UPDATE", "Order", id.toString(), "Payment status updated to " + newStatus);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/orders/{id}/refund")
    public ResponseEntity<Void> refundOrder(@PathVariable Long id) {
        CustomerOrder order = orderService.getOrderById(id);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
        logAction("REFUND", "Order", id.toString(), "Refund initiated and order cancelled");
        return ResponseEntity.ok().build();
    }

    // --- Coupons ---
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        Coupon saved = couponService.createCoupon(coupon);
        logAction("COUPON_CREATE", "Coupon", saved.getId().toString(), "Created coupon: " + saved.getCode());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        Coupon updated = couponService.updateCoupon(id, coupon);
        logAction("COUPON_UPDATE", "Coupon", id.toString(), "Updated coupon: " + updated.getCode());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        logAction("COUPON_DELETE", "Coupon", id.toString(), "Deleted coupon ID: " + id);
        return ResponseEntity.ok().build();
    }

    // --- Settings ---
    @GetMapping("/settings")
    public ResponseEntity<ShopSettings> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @RequestMapping(value = "/settings", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ShopSettings> updateSettings(@RequestBody ShopSettings settings) {
        ShopSettings updated = settingsService.updateSettings(settings);
        logAction("SETTINGS_UPDATE", "ShopSettings", "1", "Updated shop settings");
        return ResponseEntity.ok(updated);
    }

    // --- Audit Logs ---
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(auditService.getLogs(page, size, action, search));
    }
}
