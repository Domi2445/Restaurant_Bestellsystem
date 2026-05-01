package org.zimtkoriander.bestellsystem.dto;

import java.time.Instant;

public class PaymentConfigResponse {
    private Long id;
    private String provider;
    private String publicKey;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    public PaymentConfigResponse() {}

    public PaymentConfigResponse(Long id, String provider, String publicKey, boolean enabled,
                                Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.provider = provider;
        this.publicKey = publicKey;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}

