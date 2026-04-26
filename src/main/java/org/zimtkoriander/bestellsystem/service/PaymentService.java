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

    private PaymentProvider resolveProvider(Payment payment) {
        return paymentProviders.stream()
                .filter(provider -> provider.supports(payment.getMethod()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Keine Payment-Integration fur Methode " + payment.getMethod()));
    }
}

