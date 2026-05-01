package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;

public class MenuItemSizeRequest {
    private String sizeName;
    private BigDecimal priceModifier;
    private boolean available;

    public MenuItemSizeRequest() {}

    public MenuItemSizeRequest(String sizeName, BigDecimal priceModifier, boolean available) {
        this.sizeName = sizeName;
        this.priceModifier = priceModifier;
        this.available = available;
    }

    // Getters and Setters
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
}

