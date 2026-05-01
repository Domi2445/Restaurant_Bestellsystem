# 🎯 Vollständiger Leitfaden: Stripe & Klarna Integration

Dieser Leitfaden zeigt Schritt für Schritt, wie die Payment-Integration in Ihrem Restaurant-Bestellsystem funktioniert und wie Sie die Zahlungsmethoden verwalten.

## 📋 Übersicht der neuen Komponenten

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Endpoints                       │
├─────────────────────────────────────────────────────────────┤
│ Admin Configuration API:  /api/admin/payment-config/*       │
│ Payment API:              /api/payments/*                   │
│ Webhook API:              /api/webhooks/stripe              │
│                           /api/webhooks/klarna              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
├─────────────────────────────────────────────────────────────┤
│ PaymentService (Geschäftslogik)                             │
│ PaymentConfigService (Konfigurationsverwaltung)             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Payment Provider                           │
├─────────────────────────────────────────────────────────────┤
│ StripePaymentProvider                                       │
│ KlarnaPaymentProvider                                       │
│ CashOnDeliveryProvider                                      │
│ CardOnDeliveryProvider                                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                External Payment Services                    │
├─────────────────────────────────────────────────────────────┤
│ Stripe API (api.stripe.com)                                 │
│ Klarna API (api.klarna.com / api.playground.klarna.com)     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start - 5 Schritte

### 1. Database Migration
Die `PaymentConfig`-Tabelle wird automatisch von JPA Hibernate erstellt:

```sql
CREATE TABLE payment_configs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider VARCHAR(50) UNIQUE NOT NULL,
  secret_key VARCHAR(255) NOT NULL,
  public_key VARCHAR(255) NOT NULL,
  additional_config VARCHAR(255),
  enabled BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100)
);
```

### 2. Admin Benutzer erstellen
```bash
# Über Ihre vorhandene User-API
POST /api/auth/register
{
  "username": "admin",
  "email": "admin@restaurant.local",
  "password": "SecurePassword123",
  "role": "ADMIN"
}
```

### 3. JWT Token abrufen
```bash
POST /api/auth/login
{
  "username": "admin",
  "password": "SecurePassword123"
}

# Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### 4. Stripe konfigurieren
```bash
POST /api/admin/payment-config/STRIPE \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "secretKey": "sk_test_51234567890abcdefghijk",
    "publicKey": "pk_test_51234567890abcdefghijk",
    "additionalConfig": null
  }'
```

### 5. Stripe aktivieren
```bash
POST /api/admin/payment-config/STRIPE/toggle \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🔄 Payment Flow Detailliert

### Customerensicht: Bestellung bezahlen

```
1. Kunde legt Bestellung an
   POST /api/orders
   → Order mit Status: PENDING

2. Kunde initialisiert Zahlung
   POST /api/payments
   {
     "orderId": "550e8400...",
     "amount": 29.99,
     "method": "STRIPE"
   }
   
   System antwortet mit:
   {
     "id": "payment-uuid",
     "externalPaymentId": "pi_1234567890",
     "status": "PENDING",
     "provider": "STRIPE"
   }

3. Frontend nutzt Stripe.js mit externalPaymentId
   → Benutzer gibt Kartendaten ein

4. Zahlung bestätigen
   POST /api/payments/{paymentId}/confirm
   
   ✅ Wenn erfolgreich:
   - Order Status → CONFIRMED
   - Invoice wird erstellt
   - Payment Status → PAID

5. (Optional) Webhook von Stripe
   POST /api/webhooks/stripe (mit Signatur)
   → Automatische Bestätigung
```

---

## 🛠️ Architektur: Das System innen

### PaymentProvider Interface
```java
public interface PaymentProvider {
    boolean supports(PaymentMethod method);
    Payment initialize(Payment payment);
    Payment confirm(Payment payment);
}
```

Jeder Provider muss:
1. Angeben welche PaymentMethod er unterstützt
2. Eine Zahlung initialisieren (External ID erzeugen)
3. Eine Zahlung bestätigen (Status prüfen)

### PaymentConfigService
Verwaltet die Laufzeit-Konfiguration:
- Laden von Secrets aus Datenbank
- Aktivierung/Deaktivierung von Providern
- Test der Verbindung
- Sichere Secret-Verwaltung

### WebhookController
Empfängt Callbacks von Payment Providern:
- **Stripe**: Validiert Webhook-Signatur
- **Klarna**: Parst Order-Bestätigungen
- Aktualisiert Payment-Status automatisch

---

## 📊 Datenbankmodell

### Payment Entity (existiert already)
```sql
payments {
  id UUID PRIMARY KEY,
  orderId UUID,
  amount DECIMAL,
  method ENUM (CASH, CARD, STRIPE, KLARNA, ...),
  status ENUM (PENDING, PAID, FAILED, REFUNDED),
  provider VARCHAR (z.B. "STRIPE", "KLARNA"),
  externalPaymentId VARCHAR (z.B. Stripe PaymentIntent ID),
  currency VARCHAR (EUR, USD, ...),
  failureReason VARCHAR,
  paidAt TIMESTAMP,
  createdAt TIMESTAMP,
  updatedAt TIMESTAMP
}
```

### PaymentConfig Entity (neu)
```sql
payment_configs {
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider VARCHAR (z.B. "STRIPE", "KLARNA"),
  secretKey VARCHAR (verschlüsselt speichern!),
  publicKey VARCHAR,
  additionalConfig VARCHAR (z.B. "testing"/"production"),
  enabled BOOLEAN,
  createdAt TIMESTAMP,
  updatedAt TIMESTAMP,
  updatedBy VARCHAR (Admin, der es geändert hat)
}
```

---

## 🔒 Sicherheitsaspekte

### Secret Key Management

❌ **NICHT MACHEN:**
```java
payment.stripe.secret-key=sk_test_123456 // Hardcoded in application.properties
Stripe.apiKey = "sk_test_123456";         // Hardcoded in Source Code
```

✅ **RICHTIG MACHEN:**
```java
// Umgebungsvariablen:
export PAYMENT_STRIPE_SECRET_KEY=sk_test_123456

// Oder über Admin API speichern:
POST /api/admin/payment-config/STRIPE
{
  "secretKey": "sk_test_123456",  // Wird verschlüsselt gespeichert
  "publicKey": "pk_test_123456"
}
```

### Authentifizierung & Authorization

- **Nur ADMIN-Benutzer** dürfen Admin-Endpoints nutzen: `@PreAuthorize("hasRole('ADMIN')")`
- **JWT Token** muss im Header sein: `Authorization: Bearer {token}`
- **Token-Expiration** sollte konfiguriert sein

### Webhook Validation

**Stripe:** Webhook-Signatur wird validiert
```java
Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
// → Prüf ob Hash korrekt ist
```

**Klarna:** Basische Auth beim API-Aufruf
```
Authorization: Basic BASE64(merchantId:apiKey)
```

---

## 🧪 Testen im Sandbox

Die API unterscheidet zwischen `testing` und `production` Umgebung.

### Stripe Sandbox Account
```
Secret Key: sk_test_...
Publishable Key: pk_test_...
Webhook Test Cards:
  ✅ 4242 4242 4242 4242 - Zahlung erfolgreich
  ❌ 4000 0000 0000 0002 - Zahlung abgelehnt
  ⏳ 4000 0025 0000 3155 - Zahlung hängig
```

### Klarna Sandbox Account
```
API: https://api.playground.klarna.com
Merchant ID: MERCHANT_ID_TEST
API Key: API_KEY_TEST
Environment: testing
```

### Test-Curl-Sequence
```bash
# 1. Admin-Token abrufen
JWT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.accessToken')

# 2. Stripe Test-Konfiguration speichern
curl -X POST http://localhost:8080/api/admin/payment-config/STRIPE \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d @- << EOF
{
  "secretKey": "sk_test_from_stripe_dashboard",
  "publicKey": "pk_test_from_stripe_dashboard",
  "additionalConfig": null
}
EOF

# 3. Aktivieren
curl -X POST http://localhost:8080/api/admin/payment-config/STRIPE/toggle \
  -H "Authorization: Bearer $JWT"

# 4. Test-Zahlung erstellen
ORDER_ID=$(curl -s -X GET http://localhost:8080/api/orders | jq -r '.[0].id')

PAYMENT=$(curl -s -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": \"$ORDER_ID\",
    \"amount\": 29.99,
    \"method\": \"STRIPE\"
  }")

PAYMENT_ID=$(echo $PAYMENT | jq -r '.id')
STRIPE_INTENT_ID=$(echo $PAYMENT | jq -r '.externalPaymentId')

echo "Zahlung erstellt: $PAYMENT_ID"
echo "Stripe Intent: $STRIPE_INTENT_ID"

# 5. Im Stripe Dashboard manuell die Zahlung bestätigen oder via Webhook simulator
```

---

## 🚀 Production Deployment

### Environment Variables setzen
```bash
# Stripe
export PAYMENT_STRIPE_SECRET_KEY=sk_live_YOUR_PRODUCTION_KEY
export PAYMENT_STRIPE_PUBLISHABLE_KEY=pk_live_YOUR_PRODUCTION_KEY
export PAYMENT_STRIPE_WEBHOOK_SECRET=whsec_YOUR_WEBHOOK_SECRET

# Klarna
export PAYMENT_KLARNA_MERCHANT_ID=YOUR_PRODUCTION_MERCHANT_ID
export PAYMENT_KLARNA_API_KEY=YOUR_PRODUCTION_API_KEY
export PAYMENT_KLARNA_ENVIRONMENT=production
```

### Docker Compose Beispiel
```yaml
version: '3.8'
services:
  restaurant-api:
    image: restaurant-bestellsystem:latest
    environment:
      PAYMENT_STRIPE_SECRET_KEY: ${STRIPE_SECRET_KEY}
      PAYMENT_KLARNA_MERCHANT_ID: ${KLARNA_MERCHANT_ID}
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/bestellsystem
    ports:
      - "8080:8080"
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: bestellsystem
      POSTGRES_PASSWORD: secret
```

### Webhook URLs konfigurieren

**Stripe:**
1. Gehe zu: https://dashboard.stripe.com/webhooks
2. Klick "Add endpoint"
3. URL: `https://your-domain.com/api/webhooks/stripe`
4. Events: `payment_intent.succeeded`, `payment_intent.payment_failed`
5. Signing secret kopieren → in Env-Var speichern

**Klarna:**
1. Gehe zu: https://portal.payments.klarna.com/webhooks
2. Konfiguriere: `https://your-domain.com/api/webhooks/klarna`
3. Aktiviere relevante Events

---

## 📞 Support & Debugging

### Logs prüfen
```bash
# Docker logs
docker logs -f restaurant-api

# Application logs
tail -f /var/log/restaurant-api.log | grep -i payment
```

### Common Issues

| Problem | Debugging |
|---------|-----------|
| "Stripe is not enabled" | Admin API aufrufen: `POST /api/admin/payment-config/STRIPE/toggle` |
| 401 Unauthorized on webhook | Webhook-Signatur prüfen oder IP-Whitelist überprüfen |
| "External Payment ID not found" on confirm | Prüfen ob `initialize()` aufgerufen wurde |
| Payments stuck in PENDING | Webhook kann nicht erreicht werden oder fehlgeschlagen |

### Manuelles Debugging
```bash
# 1. Payment-Status überprüfen
curl -X GET http://localhost:8080/api/payments/{paymentId}

# 2. Config-Status überprüfen
curl -X GET http://localhost:8080/api/admin/payment-config/STRIPE \
  -H "Authorization: Bearer $JWT"

# 3. Verbindung testen
curl -X GET http://localhost:8080/api/admin/payment-config/STRIPE/test \
  -H "Authorization: Bearer $JWT"

# 4. Alle Payments anzeigen
curl -X GET http://localhost:8080/api/payments

# 5. DB direkt abfragen (falls möglich)
SELECT * FROM payment_configs;
SELECT * FROM payments WHERE external_payment_id = 'pi_...';
```

---

## 🎓 Weitere Ressourcen

- [Stripe API Documentation](https://stripe.com/docs/api)
- [Klarna Payments API](https://docs.payments.klarna.com/)
- [Spring Security OAuth2](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8949)
- [Payment Card Industry (PCI) Compliance](https://www.pcisecuritystandards.org/)

