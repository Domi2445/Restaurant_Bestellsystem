package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class WinOrderOrderResponse {
    private UUID orderId;
    private UUID customerId;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;

    public WinOrderOrderResponse(UUID orderId, UUID customerId, String status, BigDecimal totalAmount, Instant createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

