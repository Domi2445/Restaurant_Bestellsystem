package org.zimtkoriander.bestellsystem.payment;

import Model.Payment;
import Model.PaymentMethod;
import Model.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Component;
import org.zimtkoriander.bestellsystem.service.PaymentConfigService;

import java.time.Instant;

@Component
public class StripePaymentProvider implements PaymentProvider {

    private final PaymentConfigService paymentConfigService;

    public StripePaymentProvider(PaymentConfigService paymentConfigService) {
        this.paymentConfigService = paymentConfigService;
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.STRIPE;
    }

    @Override
    public Payment initialize(Payment payment) {
        try {
            // Prüfe ob Stripe aktiviert ist
            if (!paymentConfigService.isProviderEnabled("STRIPE")) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Stripe is not enabled");
                return payment;
            }

            // Erstelle einen PaymentIntent in Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(payment.getAmount().longValue() * 100)  // Stripe erwartet Cents
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .setDescription("Order #" + payment.getOrderId())
                    .putMetadata("orderId", payment.getOrderId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            payment.setProvider("STRIPE");
            payment.setExternalPaymentId(intent.getId());
            payment.setStatus(PaymentStatus.PENDING);

        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
        }
        return payment;
    }

    @Override
    public Payment confirm(Payment payment) {
        try {
            // Hole den PaymentIntent und überprüfe seinen Status
            PaymentIntent intent = PaymentIntent.retrieve(payment.getExternalPaymentId());

            if (intent.getStatus().equals("succeeded")) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(Instant.now());
            } else if (intent.getStatus().equals("processing")) {
                payment.setStatus(PaymentStatus.PENDING);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment intent status: " + intent.getStatus());
            }
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
        }
        return payment;
    }
}

