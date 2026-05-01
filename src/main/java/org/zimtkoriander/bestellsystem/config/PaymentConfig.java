package org.zimtkoriander.bestellsystem.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class PaymentConfig {

    @Value("${payment.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${payment.stripe.publishable-key}")
    private String stripePublishableKey;

    @Value("${payment.klarna.merchant-id}")
    private String klarnaMerchantId;

    @Value("${payment.klarna.api-key}")
    private String klarnaApiKey;

    @Bean
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String getStripeSecretKey() {
        return stripeSecretKey;
    }

    public String getStripePublishableKey() {
        return stripePublishableKey;
    }

    public String getKlarnaMerchantId() {
        return klarnaMerchantId;
    }

    public String getKlarnaApiKey() {
        return klarnaApiKey;
    }
}


