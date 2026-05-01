package org.zimtkoriander.bestellsystem.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MenuItemDetailedResponse {
    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String type;
    private boolean available;
    private List<MenuItemSizeResponse> sizes;
    private List<MenuItemAddonResponse> addons;

    public MenuItemDetailedResponse() {}

    public MenuItemDetailedResponse(UUID id, UUID storeId, String name, String description,
                                   BigDecimal basePrice, String type, boolean available,
                                   List<MenuItemSizeResponse> sizes, List<MenuItemAddonResponse> addons) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.type = type;
        this.available = available;
        this.sizes = sizes;
        this.addons = addons;
    }

    // Getters and Setters
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

    public List<MenuItemSizeResponse> getSizes() {
        return sizes;
    }

    public void setSizes(List<MenuItemSizeResponse> sizes) {
        this.sizes = sizes;
    }

    public List<MenuItemAddonResponse> getAddons() {
        return addons;
    }

    public void setAddons(List<MenuItemAddonResponse> addons) {
        this.addons = addons;
    }
}

