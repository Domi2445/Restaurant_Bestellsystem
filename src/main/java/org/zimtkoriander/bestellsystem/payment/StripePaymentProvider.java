package org.zimtkoriander.bestellsystem.payment;

import Model.Payment;
import Model.PaymentMethod;
import Model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class StripePaymentProvider implements PaymentProvider {
    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.STRIPE;
    }

    @Override
    public Payment initialize(Payment payment) {
        payment.setProvider("STRIPE");
        payment.setExternalPaymentId("stripe_" + UUID.randomUUID());
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    @Override
    public Payment confirm(Payment payment) {
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        return payment;
    }
}

