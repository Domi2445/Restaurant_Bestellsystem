package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;

public class MenuItemAddonRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer maxQuantity;
    private boolean available;

    public MenuItemAddonRequest() {}

    public MenuItemAddonRequest(String name, String description, BigDecimal price, Integer maxQuantity, boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.maxQuantity = maxQuantity;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

