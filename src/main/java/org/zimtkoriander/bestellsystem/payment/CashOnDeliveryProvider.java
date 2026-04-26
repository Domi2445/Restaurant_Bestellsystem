package org.zimtkoriander.bestellsystem.payment;

import Model.Payment;
import Model.PaymentMethod;
import Model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CashOnDeliveryProvider implements PaymentProvider {
    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CASH || method == PaymentMethod.CASH_ON_DELIVERY;
    }

    @Override
    public Payment initialize(Payment payment) {
        payment.setProvider("CASH_ON_DELIVERY");
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

