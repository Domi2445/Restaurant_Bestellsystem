package org.zimtkoriander.bestellsystem.service;

import Model.PaymentConfig;
import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zimtkoriander.bestellsystem.repository.PaymentConfigRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentConfigService {

    private final PaymentConfigRepository paymentConfigRepository;

    @Value("${payment.stripe.secret-key:}")
    private String defaultStripeSecretKey;

    @Value("${payment.stripe.publishable-key:}")
    private String defaultStripePublishableKey;

    @Value("${payment.klarna.merchant-id:}")
    private String defaultKlarnaMerchantId;

    @Value("${payment.klarna.api-key:}")
    private String defaultKlarnaApiKey;

    public PaymentConfigService(PaymentConfigRepository paymentConfigRepository) {
        this.paymentConfigRepository = paymentConfigRepository;
    }

    /**
     * Hole Konfiguration für einen Payment Provider
     */
    public PaymentConfig getConfig(String provider) {
        Optional<PaymentConfig> config = paymentConfigRepository.findByProvider(provider);
        return config.orElse(null);
    }

    /**
     * Hole nur aktivierte Konfigurationen
     */
    public PaymentConfig getEnabledConfig(String provider) {
        return paymentConfigRepository.findByProviderAndEnabled(provider, true).orElse(null);
    }

    /**
     * Speichere oder aktualisiere eine Payment-Konfiguration
     */
    public PaymentConfig saveConfig(String provider, String secretKey, String publicKey, String additionalConfig) {
        Optional<PaymentConfig> existingConfig = paymentConfigRepository.findByProvider(provider);

        PaymentConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setSecretKey(secretKey);
            config.setPublicKey(publicKey);
            config.setAdditionalConfig(additionalConfig);
        } else {
            config = new PaymentConfig(provider, secretKey, publicKey);
            config.setAdditionalConfig(additionalConfig);
        }

        config = paymentConfigRepository.save(config);

        // Aktualisiere Stripe API Key wenn Stripe konfiguriert wird
        if ("STRIPE".equalsIgnoreCase(provider)) {
            Stripe.apiKey = secretKey;
        }

        return config;
    }

    /**
     * Aktiviere/Deaktiviere einen Provider
     */
    public PaymentConfig toggleProvider(String provider, boolean enabled) {
        Optional<PaymentConfig> config = paymentConfigRepository.findByProvider(provider);

        if (config.isPresent()) {
            PaymentConfig paymentConfig = config.get();
            paymentConfig.setEnabled(enabled);
            return paymentConfigRepository.save(paymentConfig);
        }

        throw new IllegalArgumentException("Konfiguration für Provider " + provider + " nicht gefunden");
    }

    /**
     * Hole alle Konfigurationen
     */
    public List<PaymentConfig> getAllConfigs() {
        return paymentConfigRepository.findAll();
    }

    /**
     * Überprüfe, ob ein Provider aktiviert ist
     */
    public boolean isProviderEnabled(String provider) {
        return paymentConfigRepository.findByProviderAndEnabled(provider, true).isPresent();
    }

    /**
     * Lösche eine Konfiguration
     */
    public void deleteConfig(String provider) {
        Optional<PaymentConfig> config = paymentConfigRepository.findByProvider(provider);
        config.ifPresent(paymentConfigRepository::delete);
    }

    /**
     * Teste die Verbindung zu einem Payment Provider
     */
    public boolean testConnection(String provider) {
        PaymentConfig config = getConfig(provider);
        if (config == null) {
            return false;
        }

        try {
            if ("STRIPE".equalsIgnoreCase(provider)) {
                // Test Stripe API Key
                Stripe.apiKey = config.getSecretKey();
                com.stripe.model.Account.retrieve();
                return true;
            } else if ("KLARNA".equalsIgnoreCase(provider)) {
                // Test Klarna API Keys durch Basis64-Encoding
                String auth = config.getPublicKey() + ":" + config.getSecretKey();
                String encoded = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
                return !encoded.isEmpty();
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}

