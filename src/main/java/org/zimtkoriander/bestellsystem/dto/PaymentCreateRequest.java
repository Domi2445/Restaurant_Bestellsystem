package org.zimtkoriander.bestellsystem.dto;

import Model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentCreateRequest {
    private UUID orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String currency;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

