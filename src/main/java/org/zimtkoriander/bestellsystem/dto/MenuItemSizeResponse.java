package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MenuItemSizeResponse {
    private UUID id;
    private String sizeName;
    private BigDecimal priceModifier;
    private boolean available;
    private Instant createdAt;
    private Instant updatedAt;

    public MenuItemSizeResponse() {}

    public MenuItemSizeResponse(UUID id, String sizeName, BigDecimal priceModifier,
                               boolean available, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sizeName = sizeName;
        this.priceModifier = priceModifier;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public BigDecimal getPriceModifier() {
        return priceModifier;
    }

    public void setPriceModifier(BigDecimal priceModifier) {
        this.priceModifier = priceModifier;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

