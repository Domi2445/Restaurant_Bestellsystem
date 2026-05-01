package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;

public class MenuItemCreateRequest {
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String type;
    private boolean available;

    public MenuItemCreateRequest() {}

    public MenuItemCreateRequest(String name, String description, BigDecimal basePrice, String type, boolean available) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.type = type;
        this.available = available;
    }

    // Getters and Setters
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

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
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

