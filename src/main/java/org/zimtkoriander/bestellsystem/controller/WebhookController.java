package org.zimtkoriander.bestellsystem.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.service.PaymentService;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${payment.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Webhook für Stripe Payment Events
     */
    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Verifiziere die Webhook-Signatur
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().orElse(null);

                if (paymentIntent != null) {
                    // Suche die Payment und markiere sie als bezahlt
                    paymentService.confirmPaymentByExternalId(paymentIntent.getId());
                }
            }

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Webhook für Klarna Payment Events
     */
    @PostMapping("/klarna")
    public ResponseEntity<Void> handleKlarnaWebhook(@RequestBody String payload) {
        try {
            // Parse Klarna Webhook Payload und aktualisiere Payment
            // Dies ist ein Beispiel - die genaue Struktur hängt von Klarna ab
            paymentService.handleKlarnaWebhook(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

