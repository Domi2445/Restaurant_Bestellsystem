package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderResponse {
    private UUID id;
    private UUID customerId;
    private UUID storeId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;

    public OrderResponse() {
    }

    public OrderResponse(UUID id, UUID customerId, UUID storeId, BigDecimal totalAmount, String status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.storeId = storeId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}


