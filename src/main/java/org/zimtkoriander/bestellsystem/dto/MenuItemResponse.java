package org.zimtkoriander.bestellsystem.dto;

import java.util.UUID;

public class MenuItemResponse {
    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private java.math.BigDecimal basePrice;
    private String type;
    private boolean available;

    public MenuItemResponse() {
    }

    public MenuItemResponse(UUID id, UUID storeId, String name, String description, java.math.BigDecimal basePrice, String type, boolean available) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.type = type;
        this.available = available;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.math.BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(java.math.BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}


