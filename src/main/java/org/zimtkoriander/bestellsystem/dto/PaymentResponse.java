package org.zimtkoriander.bestellsystem.dto;

import Model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String method;
    private PaymentStatus status;
    private String provider;
    private String externalPaymentId;

    public PaymentResponse(UUID id, UUID orderId, BigDecimal amount, String method, PaymentStatus status, String provider, String externalPaymentId) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.provider = provider;
        this.externalPaymentId = externalPaymentId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getProvider() {
        return provider;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }
}

