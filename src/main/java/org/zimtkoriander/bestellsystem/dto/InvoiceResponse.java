package org.zimtkoriander.bestellsystem.dto;

import Model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class InvoiceResponse {
    private UUID id;
    private UUID orderId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String email;
    private InvoiceStatus status;
    private Instant createdAt;
    private Instant sentAt;

    public InvoiceResponse(UUID id, UUID orderId, String invoiceNumber, BigDecimal amount, String email, InvoiceStatus status, Instant createdAt, Instant sentAt) {
        this.id = id;
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getEmail() {
        return email;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}

