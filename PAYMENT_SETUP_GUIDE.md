# Zahlung Integration Guide - Stripe & Klarna

## 🔑 API-Keys erhalten

### Stripe Setup

1. **Account erstellen**:
   - Gehe zu https://stripe.com
   - Erstelle einen kostenlosen Account
   - Gehe zum Dashboard → Developers → API Keys

2. **Test Keys kopieren**:
   ```
   Secret Key: sk_test_****
   Publishable Key: pk_test_****
   ```

3. **Webhook Secret generieren**:
   ```
   Developers → Webhooks → Endpoint hinzufügen
   URL: https://yourserver.com/api/webhooks/stripe
   Events: payment_intent.succeeded, payment_intent.payment_failed
   ```

4. **In `application.properties` eintragen**:
   ```properties
   payment.stripe.secret-key=sk_test_YOUR_KEY
   payment.stripe.publishable-key=pk_test_YOUR_KEY
   payment.stripe.webhook-secret=whsec_YOUR_WEBHOOK_SECRET
   ```

---

### Klarna Setup

1. **Klarna Merchant Account**:
   - Account: https://portal.payments.klarna.com
   - Gehe zu Settings → API Credentials

2. **Credentials kopieren**:
   ```
   Merchant ID: MERCHANT_ID
   API Key: API_PASSWORD
   ```

3. **In `application.properties` eintragen**:
   ```properties
   payment.klarna.merchant-id=YOUR_MERCHANT_ID
   payment.klarna.api-key=YOUR_API_KEY
   payment.klarna.environment=testing
   ```

---

## 🧪 Testen im Sandbox-Modus

### Stripe Test-Kartennummern
```
Erfolgreiche Zahlung:    4242 4242 4242 4242
Zahlung abgelehnt:       4000 0000 0000 0002
Weitere Test Cards:      https://stripe.com/docs/testing
```

### Klarna Test-Daten
```
Erfolgreich:   Alle Test-Beträge akzeptieren
Abgelehnt:     Beträge > 1€ können abgelehnt werden
```

---

## 📋 API Endpoints

### Payment erstellen
```bash
POST /api/payments
Content-Type: application/json

{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 29.99,
  "method": "STRIPE"
}
```

### Payment bestätigen
```bash
POST /api/payments/{paymentId}/confirm
```

### Payment Status abrufen
```bash
GET /api/payments/{paymentId}
```

---

## 🔄 Payment Flow

1. **Zahlung initialisieren**: POST `/api/payments`
   - System erstellt PaymentIntent bei Stripe/Klarna
   - Rückgabe: `externalPaymentId` zum Frontend schicken

2. **Frontend**: Nutzer gibt Zahlungsdaten ein (Stripe.js/Klarna Widget)

3. **Zahlung bestätigen**: POST `/api/payments/{paymentId}/confirm`
   - System prüft Status bei Payment Provider
   - Status aktualisiert: PENDING → PAID/FAILED

4. **Webhook**: Payment Provider sendet Bestätigung
   - System empfängt Webhook
   - Payment automatisch aktualisiert

---

## 🚀 Production Deployment

### Sicherheit:
- **API Keys**: Verwende Umgebungsvariablen, NIEMALS in Code hardcodieren
- **HTTPS**: Webhooks IMMER über HTTPS
- **Secret Verification**: Webhook-Signatur immer prüfen

### Umgebungsvariablen (z.B. Docker):
```bash
export PAYMENT_STRIPE_SECRET_KEY=sk_live_YOUR_PROD_KEY
export PAYMENT_STRIPE_PUBLISHABLE_KEY=pk_live_YOUR_PROD_KEY
export PAYMENT_KLARNA_MERCHANT_ID=YOUR_PROD_ID
export PAYMENT_KLARNA_API_KEY=YOUR_PROD_KEY
export PAYMENT_KLARNA_ENVIRONMENT=production
```

---

## 🐛 Troubleshooting

| Problem | Lösung |
|---------|--------|
| 401 Unauthorized | API Keys überprüfen, korrekte Basis64-Codierung für Klarna |
| Invalid Webhook Secret | Webhook Secret aus Stripe Dashboard neu kopieren |
| Payment Status FAILED | Logs checken, failure_reason in DB prüfen |
| Connection Timeout | API Endpoints überprüfen (Test vs. Production) |

---

## 📚 Weitere Ressourcen

- **Stripe Docs**: https://stripe.com/docs/payments/payment-intents
- **Klarna Docs**: https://docs.payments.klarna.com/
- **Spring Security**: https://spring.io/projects/spring-security

