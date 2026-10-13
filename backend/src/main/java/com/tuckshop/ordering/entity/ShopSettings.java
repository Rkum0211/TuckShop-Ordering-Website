package com.tuckshop.ordering.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_settings")
@Getter
@Setter
public class ShopSettings {

    @Id
    private Long id = 1L; // Singleton pattern in DB

    @Column(nullable = false, length = 100)
    private String shopName = "Campus Tuck Shop";

    @Column(length = 150)
    private String pickupLocation = "Counter 1, Main Tuck Shop";

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = true;

    @Column(nullable = false, length = 10)
    private String openingTime = "08:00";

    @Column(nullable = false, length = 10)
    private String closingTime = "22:00";

    @Column(nullable = false)
    private Integer dailyOrderLimit = 200;

    @Column(nullable = false)
    private Integer estimatedPreparationMinutes = 10;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.valueOf(20);

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxOrderAmount = BigDecimal.valueOf(2000);

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
