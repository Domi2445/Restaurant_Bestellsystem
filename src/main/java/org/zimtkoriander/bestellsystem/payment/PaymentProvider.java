package org.zimtkoriander.bestellsystem.payment;

import Model.Payment;
import Model.PaymentMethod;

public interface PaymentProvider {
    boolean supports(PaymentMethod method);

    Payment initialize(Payment payment);

    Payment confirm(Payment payment);
}

