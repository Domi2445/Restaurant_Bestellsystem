package org.zimtkoriander.bestellsystem.payment;

import Model.Payment;
import Model.PaymentConfig;
import Model.PaymentMethod;
import Model.PaymentStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zimtkoriander.bestellsystem.service.PaymentConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;

@Component
public class KlarnaPaymentProvider implements PaymentProvider {
    
    private final PaymentConfigService paymentConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String API_URL_TESTING = "https://api.playground.klarna.com";
    private static final String API_URL_PROD = "https://api.klarna.com";

    public KlarnaPaymentProvider(PaymentConfigService paymentConfigService) {
        this.paymentConfigService = paymentConfigService;
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.KLARNA;
    }

    @Override
    public Payment initialize(Payment payment) {
        try {
            // Prüfe ob Klarna aktiviert ist
            if (!paymentConfigService.isProviderEnabled("KLARNA")) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Klarna is not enabled");
                return payment;
            }

            PaymentConfig config = paymentConfigService.getEnabledConfig("KLARNA");
            if (config == null) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Klarna configuration not found");
                return payment;
            }

            String environment = config.getAdditionalConfig() != null ? config.getAdditionalConfig() : "testing";
            String apiUrl = "production".equalsIgnoreCase(environment) ? API_URL_PROD : API_URL_TESTING;

            // Erstelle einen Klarna Session
            String sessionPayload = createSessionPayload(payment);
            
            HttpHeaders headers = createAuthHeaders(config.getPublicKey(), config.getSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(sessionPayload, headers);
            
            String response = restTemplate.postForObject(
                    apiUrl + "/checkout/v3/sessions",
                    request,
                    String.class
            );
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            String sessionId = jsonResponse.get("session_id").asText();
            
            payment.setProvider("KLARNA");
            payment.setExternalPaymentId(sessionId);
            payment.setStatus(PaymentStatus.PENDING);
            
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Klarna initialization failed: " + e.getMessage());
        }
        return payment;
    }

    @Override
    public Payment confirm(Payment payment) {
        try {
            PaymentConfig config = paymentConfigService.getEnabledConfig("KLARNA");
            if (config == null) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Klarna configuration not found");
                return payment;
            }

            String environment = config.getAdditionalConfig() != null ? config.getAdditionalConfig() : "testing";
            String apiUrl = "production".equalsIgnoreCase(environment) ? API_URL_PROD : API_URL_TESTING;

            HttpHeaders headers = createAuthHeaders(config.getPublicKey(), config.getSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            // Hole Session-Details
            String response = restTemplate.getForObject(
                    apiUrl + "/checkout/v3/sessions/" + payment.getExternalPaymentId(),
                    String.class
            );
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            String status = jsonResponse.get("order_status").asText();
            
            if ("checkout_complete".equals(status)) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(Instant.now());
            } else if ("checkout_incomplete".equals(status)) {
                payment.setStatus(PaymentStatus.PENDING);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Klarna order status: " + status);
            }
            
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Klarna confirmation failed: " + e.getMessage());
        }
        return payment;
    }

    private HttpHeaders createAuthHeaders(String merchantId, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        String auth = merchantId + ":" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    private String createSessionPayload(Payment payment) throws Exception {
        return objectMapper.writeValueAsString(new Object() {
            public final String purchase_country = "DE";
            public final String purchase_currency = payment.getCurrency();
            public final String locale = "de-DE";
            public final OrderAmount order_amount = new OrderAmount(
                    payment.getAmount().longValue() * 100,
                    payment.getAmount().longValue() * 100,
                    0,
                    0
            );
            
            public static class OrderAmount {
                public long order_amount;
                public long order_tax_amount;
                public long shipping_amount;
                public long order_line_items_amount;
                
                public OrderAmount(long order_amount, long order_line_items_amount, 
                                 long shipping_amount, long order_tax_amount) {
                    this.order_amount = order_amount;
                    this.order_line_items_amount = order_line_items_amount;
                    this.shipping_amount = shipping_amount;
                    this.order_tax_amount = order_tax_amount;
                }
            }
        });
    }
}

