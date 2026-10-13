package com.tuckshop.ordering.controller;

import com.tuckshop.ordering.dto.OrderRequest;
import com.tuckshop.ordering.entity.Coupon;
import com.tuckshop.ordering.entity.CustomerOrder;
import com.tuckshop.ordering.entity.DiscountType;
import com.tuckshop.ordering.entity.Product;
import com.tuckshop.ordering.entity.ShopSettings;
import com.tuckshop.ordering.exception.BadRequestException;
import com.tuckshop.ordering.service.CouponService;
import com.tuckshop.ordering.service.OrderService;
import com.tuckshop.ordering.service.PaymentService;
import com.tuckshop.ordering.service.ProductService;
import com.tuckshop.ordering.service.SettingsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PublicController {

    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final SettingsService settingsService;

    public PublicController(ProductService productService, OrderService orderService, PaymentService paymentService, CouponService couponService, SettingsService settingsService) {
        this.productService = productService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.couponService = couponService;
        this.settingsService = settingsService;
    }

    @GetMapping({"/api/shop-status", "/api/public/shop-status"})
    public ResponseEntity<ShopSettings> getShopStatus() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @GetMapping({"/api/categories", "/api/public/categories"})
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getCategories();
        if (categories == null || categories.isEmpty()) {
            categories = List.of("Snacks", "Drinks", "Bakery", "Chocolates", "Stationery");
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping({"/api/products", "/api/public/products"})
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, category, true, search));
    }

    @GetMapping({"/api/products/{id}", "/api/public/products/{id}"})
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping({"/api/coupons/validate/{code}", "/api/public/coupons/validate/{code}"})
    public ResponseEntity<Coupon> validateCouponByCode(@PathVariable String code) {
        Coupon coupon = couponService.getCouponByCode(code);
        if (!coupon.getActive()) {
            throw new BadRequestException("Coupon is currently inactive");
        }
        return ResponseEntity.ok(coupon);
    }

    @PostMapping({"/api/coupons/validate", "/api/public/coupons/validate"})
    public ResponseEntity<Map<String, Object>> validateCouponPost(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Coupon code is required");
        }

        BigDecimal orderTotal = BigDecimal.ZERO;
        if (body.get("orderTotal") != null) {
            orderTotal = new BigDecimal(body.get("orderTotal").toString());
        }

        Coupon coupon = couponService.getCouponByCode(code.trim());
        if (!coupon.getActive()) {
            throw new BadRequestException("Coupon is no longer active");
        }

        if (coupon.getMinimumOrderAmount() != null && orderTotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order of ₹" + coupon.getMinimumOrderAmount() + " required for this coupon");
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaximumDiscount() != null && discount.compareTo(coupon.getMaximumDiscount()) > 0) {
                discount = coupon.getMaximumDiscount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("code", coupon.getCode());
        response.put("discountAmount", discount);
        response.put("discountType", coupon.getDiscountType().name());
        response.put("discountValue", coupon.getDiscountValue());
        response.put("message", "Coupon applied successfully!");

        return ResponseEntity.ok(response);
    }

    @PostMapping({"/api/orders", "/api/public/orders"})
    public ResponseEntity<CustomerOrder> createOrder(@RequestBody OrderRequest request) {
        CustomerOrder order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping({"/api/orders/track", "/api/public/orders/track"})
    public ResponseEntity<CustomerOrder> trackOrder(
            @RequestParam String token,
            @RequestParam(required = false) String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            return ResponseEntity.ok(orderService.getOrderByTokenAndPhone(token.trim(), phone.trim()));
        }
        return ResponseEntity.ok(orderService.getOrderByTokenAndPhone(token.trim(), ""));
    }

    @GetMapping({"/api/orders/{token}", "/api/public/orders/{token}"})
    public ResponseEntity<CustomerOrder> getOrderByToken(
            @PathVariable String token,
            @RequestParam(required = false, defaultValue = "") String phoneNumber) {
        return ResponseEntity.ok(orderService.getOrderByTokenAndPhone(token.trim(), phoneNumber.trim()));
    }

    @PostMapping({"/api/payments/create-order", "/api/payments/create-order/{orderId}",
                  "/api/public/payments/create-order", "/api/public/payments/create-order/{orderId}"})
    public ResponseEntity<Map<String, Object>> createPaymentOrder(
            @PathVariable(required = false) Long orderId,
            @RequestBody(required = false) Map<String, Object> body) {
        Long targetOrderId = orderId;
        if (targetOrderId == null && body != null) {
            if (body.get("customerOrderId") != null) {
                targetOrderId = Long.valueOf(body.get("customerOrderId").toString());
            } else if (body.get("orderId") != null) {
                targetOrderId = Long.valueOf(body.get("orderId").toString());
            }
        }
        if (targetOrderId == null) {
            throw new BadRequestException("Order ID is required to create payment");
        }

        Map<String, Object> response = paymentService.createPaymentOrder(targetOrderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/api/payments/verify", "/api/public/payments/verify"})
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody Map<String, Object> payload) {
        String rzpOrderId = payload.get("razorpayOrderId") != null ? payload.get("razorpayOrderId").toString() : null;
        String rzpPaymentId = payload.get("razorpayPaymentId") != null ? payload.get("razorpayPaymentId").toString() : null;
        String signature = payload.get("razorpaySignature") != null ? payload.get("razorpaySignature").toString() : null;
        Long customerOrderId = payload.get("customerOrderId") != null ? Long.valueOf(payload.get("customerOrderId").toString()) : null;

        paymentService.verifyPayment(rzpOrderId, rzpPaymentId, signature, customerOrderId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment verified and order confirmed!");
        return ResponseEntity.ok(response);
    }
}
