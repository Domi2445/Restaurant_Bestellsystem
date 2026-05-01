package org.zimtkoriander.bestellsystem.dto;

public class PaymentConfigRequest {
    private String secretKey;
    private String publicKey;
    private String additionalConfig;

    public PaymentConfigRequest() {}

    public PaymentConfigRequest(String secretKey, String publicKey, String additionalConfig) {
        this.secretKey = secretKey;
        this.publicKey = publicKey;
        this.additionalConfig = additionalConfig;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }
}

