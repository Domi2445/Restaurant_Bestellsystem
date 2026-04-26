package org.zimtkoriander.bestellsystem.service;

import Model.Invoice;
import Model.InvoiceStatus;
import Model.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zimtkoriander.bestellsystem.repository.InvoiceRepository;
import org.zimtkoriander.bestellsystem.repository.OrderRepository;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderRepository orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Invoice createInvoiceForOrder(UUID orderId, String email) {
        return invoiceRepository.findByOrderId(orderId).orElseGet(() -> {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Bestellung nicht gefunden."));

            Invoice invoice = new Invoice();
            invoice.setOrderId(order.getId());
            invoice.setAmount(order.getTotalAmount());
            invoice.setEmail(email == null || email.isBlank() ? "rechnung@bestellsystem.local" : email);
            invoice.setInvoiceNumber(buildInvoiceNumber(order.getId()));
            invoice.setStatus(InvoiceStatus.CREATED);
            return invoiceRepository.save(invoice);
        });
    }

    @Transactional
    public Invoice sendInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Rechnung nicht gefunden."));

        // Platzhalter fur echten Mailversand / E-Rechnungs-Export.
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setSentAt(Instant.now());
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Rechnung nicht gefunden."));
    }

    public Invoice getInvoiceByOrderId(UUID orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Rechnung fur Bestellung nicht gefunden."));
    }

    private String buildInvoiceNumber(UUID orderId) {
        String datePart = DateTimeFormatter.ofPattern("yyyyMMdd").format(Instant.now().atZone(java.time.ZoneId.systemDefault()));
        return "INV-" + datePart + "-" + orderId.toString().substring(0, 8).toUpperCase();
    }
}

