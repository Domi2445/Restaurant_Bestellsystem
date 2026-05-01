package org.zimtkoriander.bestellsystem.controller;

import Model.PaymentConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.dto.PaymentConfigRequest;
import org.zimtkoriander.bestellsystem.dto.PaymentConfigResponse;
import org.zimtkoriander.bestellsystem.service.PaymentConfigService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin API für Payment-Konfigurationen (Stripe, Klarna, etc.)
 * Benötigt ADMIN-Rolle für Zugriff
 */
@RestController
@RequestMapping("/api/admin/payment-config")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentConfigController {

    private final PaymentConfigService paymentConfigService;

    public PaymentConfigController(PaymentConfigService paymentConfigService) {
        this.paymentConfigService = paymentConfigService;
    }

    /**
     * Abrufen aller Payment-Konfigurationen
     */
    @GetMapping
    public ResponseEntity<List<PaymentConfigResponse>> getAllConfigs() {
        List<PaymentConfig> configs = paymentConfigService.getAllConfigs();
        List<PaymentConfigResponse> responses = configs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Abrufen einer spezifischen Payment-Konfiguration
     */
    @GetMapping("/{provider}")
    public ResponseEntity<PaymentConfigResponse> getConfig(@PathVariable String provider) {
        PaymentConfig config = paymentConfigService.getConfig(provider);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(config));
    }

    /**
     * Erstelle oder aktualisiere eine Payment-Konfiguration
     *
     * Beispiel-Request für Stripe:
     * {
     *   "publicKey": "pk_test_YOUR_KEY",
     *   "secretKey": "sk_test_YOUR_KEY",
     *   "additionalConfig": null
     * }
     *
     * Beispiel-Request für Klarna:
     * {
     *   "publicKey": "MERCHANT_ID",
     *   "secretKey": "API_KEY",
     *   "additionalConfig": "testing"
     * }
     */
    @PostMapping("/{provider}")
    public ResponseEntity<PaymentConfigResponse> saveConfig(
            @PathVariable String provider,
            @RequestBody PaymentConfigRequest request) {

        if (request.getSecretKey() == null || request.getSecretKey().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getPublicKey() == null || request.getPublicKey().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PaymentConfig config = paymentConfigService.saveConfig(
                    provider.toUpperCase(),
                    request.getSecretKey(),
                    request.getPublicKey(),
                    request.getAdditionalConfig()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(config));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Aktualisiere nur die Secrets (ohne öffentliche Keys zu expo)
     */
    @PutMapping("/{provider}")
    public ResponseEntity<PaymentConfigResponse> updateConfig(
            @PathVariable String provider,
            @RequestBody PaymentConfigRequest request) {

        PaymentConfig existingConfig = paymentConfigService.getConfig(provider);
        if (existingConfig == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            PaymentConfig config = paymentConfigService.saveConfig(
                    provider.toUpperCase(),
                    request.getSecretKey(),
                    request.getPublicKey(),
                    request.getAdditionalConfig()
            );
            return ResponseEntity.ok(toResponse(config));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Aktiviere oder deaktiviere einen Payment Provider
     */
    @PostMapping("/{provider}/toggle")
    public ResponseEntity<PaymentConfigResponse> toggleProvider(
            @PathVariable String provider) {

        try {
            PaymentConfig config = paymentConfigService.getConfig(provider);
            if (config == null) {
                return ResponseEntity.notFound().build();
            }

            PaymentConfig updated = paymentConfigService.toggleProvider(
                    provider.toUpperCase(),
                    !config.isEnabled()
            );
            return ResponseEntity.ok(toResponse(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Teste die Verbindung zu einem Payment Provider
     */
    @GetMapping("/{provider}/test")
    public ResponseEntity<TestConnectionResponse> testConnection(@PathVariable String provider) {
        try {
            boolean success = paymentConfigService.testConnection(provider);
            return ResponseEntity.ok(new TestConnectionResponse(success));
        } catch (Exception e) {
            return ResponseEntity.ok(new TestConnectionResponse(false));
        }
    }

    /**
     * Lösche eine Payment-Konfiguration
     */
    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String provider) {
        try {
            paymentConfigService.deleteConfig(provider.toUpperCase());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Konvertiere PaymentConfig zu Response ohne Secret Keys
     */
    private PaymentConfigResponse toResponse(PaymentConfig config) {
        return new PaymentConfigResponse(
                config.getId(),
                config.getProvider(),
                config.getPublicKey(),
                config.isEnabled(),
                config.getCreatedAt(),
                config.getUpdatedAt(),
                config.getUpdatedBy()
        );
    }

    /**
     * Response Klasse für Test-Verbindungen
     */
    public static class TestConnectionResponse {
        private boolean success;
        private String message;

        public TestConnectionResponse(boolean success) {
            this.success = success;
            this.message = success ? "Connection successful" : "Connection failed";
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}

