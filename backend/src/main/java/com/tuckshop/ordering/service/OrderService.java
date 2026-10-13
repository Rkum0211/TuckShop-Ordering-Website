package com.tuckshop.ordering.service;

import com.tuckshop.ordering.dto.OrderRequest;
import com.tuckshop.ordering.entity.*;
import com.tuckshop.ordering.exception.BadRequestException;
import com.tuckshop.ordering.exception.ResourceNotFoundException;
import com.tuckshop.ordering.repository.CustomerOrderRepository;
import com.tuckshop.ordering.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponService couponService;
    private final SettingsService settingsService;

    // In a real prod environment, use Redis or a DB sequence for daily tokens
    private static final AtomicInteger dailyTokenCounter = new AtomicInteger(1000);

    public OrderService(CustomerOrderRepository orderRepository, ProductRepository productRepository, CouponService couponService, SettingsService settingsService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.couponService = couponService;
        this.settingsService = settingsService;
    }

    @Transactional
    public CustomerOrder createOrder(OrderRequest request) {
        ShopSettings settings = settingsService.getSettings();
        // Here we would check if shop is open based on time, skipping for simplicity

        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(request.getCustomerName());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setPickupLocation(request.getPickupLocation());
        order.setNotes(request.getNotes());
        order.setTokenNumber("TS-" + dailyTokenCounter.incrementAndGet());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product ID " + itemReq.getProductId() + " not found"));

            if (!product.getAvailable()) {
                throw new BadRequestException("Product " + product.getName() + " is currently unavailable.");
            }
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemReq.getQuantity());

            BigDecimal unitPrice = product.getDiscountedPrice() != null ? product.getDiscountedPrice() : product.getPrice();
            orderItem.setUnitPrice(unitPrice);

            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderItem.setTotalPrice(totalPrice);
            
            subtotal = subtotal.add(totalPrice);
            items.add(orderItem);
        }

        order.setItems(items);
        order.setSubtotal(subtotal);

        // Apply Coupon
        BigDecimal discount = BigDecimal.ZERO;
        if (StringUtils.hasText(request.getCouponCode())) {
            try {
                Coupon coupon = couponService.getCouponByCode(request.getCouponCode());
                if (coupon.getActive() && (coupon.getMinimumOrderAmount() == null || subtotal.compareTo(coupon.getMinimumOrderAmount()) >= 0)) {
                    if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                        discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        if (coupon.getMaximumDiscount() != null && discount.compareTo(coupon.getMaximumDiscount()) > 0) {
                            discount = coupon.getMaximumDiscount();
                        }
                    } else {
                        discount = coupon.getDiscountValue();
                    }
                    order.setAppliedCoupon(coupon);
                }
            } catch (ResourceNotFoundException ignored) {}
        }

        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal.subtract(discount).max(BigDecimal.ZERO));

        if (order.getTotalAmount().compareTo(settings.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount is ₹" + settings.getMinOrderAmount());
        }
        if (order.getTotalAmount().compareTo(settings.getMaxOrderAmount()) > 0) {
            throw new BadRequestException("Maximum order amount is ₹" + settings.getMaxOrderAmount());
        }

        PaymentMethod method = PaymentMethod.CASH_ON_PICKUP;
        if (StringUtils.hasText(request.getPaymentMethod())) {
            try {
                method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
            } catch (Exception ignored) {}
        }
        order.setPaymentMethod(method);

        if (method == PaymentMethod.CASH_ON_PICKUP || method == PaymentMethod.UPI_QR) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else if (method == PaymentMethod.DEMO) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderByTokenAndPhone(String token, String phone) {
        if (StringUtils.hasText(phone)) {
            return orderRepository.findByTokenNumberAndPhoneNumber(token, phone)
                    .orElseGet(() -> orderRepository.findByTokenNumber(token)
                            .orElseThrow(() -> new ResourceNotFoundException("Order not found with token: " + token)));
        }
        return orderRepository.findByTokenNumber(token)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with token: " + token));
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrder> getAllOrders(int page, int size, OrderStatus status, PaymentStatus paymentStatus, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findFilteredOrders(status, paymentStatus, search, pageable);
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional
    public CustomerOrder updateOrderStatus(Long id, OrderStatus status) {
        CustomerOrder order = getOrderById(id);
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public CustomerOrder updatePaymentStatus(Long id, PaymentStatus status) {
        CustomerOrder order = getOrderById(id);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getDashboardStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);

        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);

        Double todaySales = orderRepository.sumSalesToday(startOfDay, endOfDay);
        if (todaySales == null) {
            Double totalSales = orderRepository.sumTotalSales();
            todaySales = totalSales != null ? totalSales : 0.0;
        }
        stats.put("todaySales", todaySales.intValue());

        stats.put("pendingOrders", orderRepository.countByOrderStatus(OrderStatus.PENDING));
        stats.put("confirmedOrders", orderRepository.countByOrderStatus(OrderStatus.CONFIRMED));
        stats.put("preparingOrders", orderRepository.countByOrderStatus(OrderStatus.PREPARING));
        stats.put("readyOrders", orderRepository.countByOrderStatus(OrderStatus.READY_FOR_PICKUP));
        stats.put("completedOrders", orderRepository.countByOrderStatus(OrderStatus.COMPLETED));
        stats.put("cancelledOrders", orderRepository.countByOrderStatus(OrderStatus.CANCELLED));

        Pageable top10 = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<CustomerOrder> recentOrders = orderRepository.findAll(top10).getContent();
        stats.put("recentOrders", recentOrders);

        // 7-day trend labels & values
        List<String> dailyLabels = new ArrayList<>();
        List<Integer> dailyValues = new ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("EEE");

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
            dailyLabels.add(day.format(fmt));
            Double daySales = orderRepository.sumSalesToday(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
            dailyValues.add(daySales != null ? daySales.intValue() : 0);
        }
        stats.put("dailyLabels", dailyLabels);
        stats.put("dailyValues", dailyValues);

        return stats;
    }
}
