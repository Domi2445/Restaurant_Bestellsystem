package org.zimtkoriander.bestellsystem.service;

import Model.Order;
import Model.OrderStatus;
import Model.Payment;
import Model.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zimtkoriander.bestellsystem.dto.PaymentCreateRequest;
import org.zimtkoriander.bestellsystem.payment.PaymentProvider;
import org.zimtkoriander.bestellsystem.repository.OrderRepository;
import org.zimtkoriander.bestellsystem.repository.PaymentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;
    private final List<PaymentProvider> paymentProviders;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          InvoiceService invoiceService,
                          List<PaymentProvider> paymentProviders) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.invoiceService = invoiceService;
        this.paymentProviders = paymentProviders;
    }

    @Transactional
    public Payment createPayment(PaymentCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Bestellung nicht gefunden."));

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setAmount(request.getAmount() == null ? order.getTotalAmount() : request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setCurrency(request.getCurrency() == null || request.getCurrency().isBlank() ? "EUR" : request.getCurrency().toUpperCase());
        payment.setStatus(PaymentStatus.PENDING);

        PaymentProvider provider = resolveProvider(payment);
        Payment initialized = provider.initialize(payment);
        return paymentRepository.save(initialized);
    }

    @Transactional
    public Payment confirmPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Zahlung nicht gefunden."));

        PaymentProvider provider = resolveProvider(payment);
        Payment confirmed = provider.confirm(payment);
        Payment saved = paymentRepository.save(confirmed);

        if (saved.getStatus() == PaymentStatus.PAID) {
            Order order = orderRepository.findById(saved.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Bestellung nicht gefunden."));
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            invoiceService.createInvoiceForOrder(order.getId(), "rechnung@bestellsystem.local");
        }

        return saved;
    }

    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Zahlung nicht gefunden."));
    }

    /**
     * Bestätige Zahlung via externe ID (z.B. Stripe PaymentIntent ID)
     * Wird durch Webhooks aufgerufen
     */
    @Transactional
    public void confirmPaymentByExternalId(String externalPaymentId) {
        paymentRepository.findByExternalPaymentId(externalPaymentId)
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.PAID);
                    paymentRepository.save(payment);

                    // Aktualisiere Order Status
                    Order order = orderRepository.findById(payment.getOrderId())
                            .orElse(null);
                    if (order != null) {
                        order.setStatus(OrderStatus.CONFIRMED);
                        orderRepository.save(order);
                    }
                });
    }

    /**
     * Verarbeite Klarna Webhook Payload
     */
    @Transactional
    public void handleKlarnaWebhook(String payload) {
        // Implementierung hängt von Klarna Webhook Struktur ab
        // Beispiel: Parse JSON und aktualisiere Payment Status
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(payload);

            String orderId = root.get("order_id").asText();
            String status = root.get("status").asText();

            paymentRepository.findByExternalPaymentId(orderId)
                    .ifPresent(payment -> {
                        if ("completed".equalsIgnoreCase(status)) {
                            payment.setStatus(PaymentStatus.PAID);
                            paymentRepository.save(payment);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Fehler beim Verarbeiten von Klarna Webhook: " + e.getMessage());
        }
    }

    private PaymentProvider resolveProvider(Payment payment) {
        return paymentProviders.stream()
                .filter(provider -> provider.supports(payment.getMethod()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Keine Payment-Integration fur Methode " + payment.getMethod()));
    }
}

