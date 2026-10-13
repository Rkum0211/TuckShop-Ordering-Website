package com.tuckshop.ordering.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String customerName;
    private String phoneNumber;
    private String pickupLocation;
    private String notes;
    private String couponCode;
    private String paymentMethod;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
