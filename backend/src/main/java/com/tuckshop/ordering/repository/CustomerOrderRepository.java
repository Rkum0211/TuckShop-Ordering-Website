package com.tuckshop.ordering.repository;

import com.tuckshop.ordering.entity.CustomerOrder;
import com.tuckshop.ordering.entity.OrderStatus;
import com.tuckshop.ordering.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByTokenNumberAndPhoneNumber(String tokenNumber, String phoneNumber);

    Optional<CustomerOrder> findByTokenNumber(String tokenNumber);

    Optional<CustomerOrder> findByRazorpayOrderId(String razorpayOrderId);

    @Query("SELECT o FROM CustomerOrder o WHERE " +
           "(:status IS NULL OR o.orderStatus = :status) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:search IS NULL OR LOWER(o.tokenNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CustomerOrder> findFilteredOrders(
            @Param("status") OrderStatus status,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    long countOrdersToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    long countByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT SUM(o.totalAmount) FROM CustomerOrder o WHERE o.paymentStatus = 'PAID' AND o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    Double sumSalesToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT SUM(o.totalAmount) FROM CustomerOrder o WHERE o.paymentStatus = 'PAID'")
    Double sumTotalSales();
}
