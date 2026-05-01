au# Payment Konfiguration über Admin API

Die Payment-Provider (Stripe & Klarna) können nun zur Laufzeit über REST API konfiguriert werden, ohne den Code oder die `application.properties` zu ändern.

## 🔐 Authentifizierung

Alle Endpoints benötigen:
- **ADMIN-Rolle** (Middleware: `@PreAuthorize("hasRole('ADMIN')")`)
- **JWT Token** im Header: `Authorization: Bearer YOUR_JWT_TOKEN`

## 📋 API Endpoints

### 1️⃣ Alle Konfigurationen abrufen

```bash
GET /api/admin/payment-config
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
[
  {
    "id": 1,
    "provider": "STRIPE",
    "publicKey": "pk_test_...",
    "enabled": true,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z",
    "updatedBy": "admin@example.com"
  },
  {
    "id": 2,
    "provider": "KLARNA",
    "publicKey": "MERCHANT_ID",
    "enabled": false,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z",
    "updatedBy": null
  }
]
```

---

### 2️⃣ Spezifische Konfiguration abrufen

```bash
GET /api/admin/payment-config/{provider}
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/payment-config/STRIPE
```

**Response:**
```json
{
  "id": 1,
  "provider": "STRIPE",
  "publicKey": "pk_test_...",
  "enabled": true,
  "createdAt": "2026-05-01T10:00:00Z",
  "updatedAt": "2026-05-01T10:00:00Z",
  "updatedBy": "admin@example.com"
}
```

---

### 3️⃣ Konfiguration speichern/aktualisieren

```bash
POST /api/admin/payment-config/{provider}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "secretKey": "sk_test_YOUR_SECRET_KEY",
  "publicKey": "pk_test_YOUR_PUBLIC_KEY",
  "additionalConfig": null
}
```

**Beispiele:**

#### Stripe konfigurieren:
```bash
POST /api/admin/payment-config/STRIPE
{
  "secretKey": "sk_test_51234567890abcdef",
  "publicKey": "pk_test_51234567890abcdef",
  "additionalConfig": null
}
```

#### Klarna konfigurieren:
```bash
POST /api/admin/payment-config/KLARNA
{
  "secretKey": "YOUR_API_KEY",
  "publicKey": "YOUR_MERCHANT_ID",
  "additionalConfig": "testing"  // oder "production"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "provider": "STRIPE",
  "publicKey": "pk_test_...",
  "enabled": false,
  "createdAt": "2026-05-01T10:00:00Z",
  "updatedAt": "2026-05-01T10:00:00Z",
  "updatedBy": null
}
```

---

### 4️⃣ Konfiguration aktualisieren (PUT)

```bash
PUT /api/admin/payment-config/{provider}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "secretKey": "sk_test_NEW_KEY",
  "publicKey": "pk_test_NEW_KEY",
  "additionalConfig": null
}
```

**Response:** `200 OK` with updated config

---

### 5️⃣ Provider aktivieren/deaktivieren

```bash
POST /api/admin/payment-config/{provider}/toggle
Authorization: Bearer {JWT_TOKEN}
```

This toggles the `enabled` status (true ↔ false).

**Response:**
```json
{
  "id": 1,
  "provider": "STRIPE",
  "publicKey": "pk_test_...",
  "enabled": true,  // ✅ Wurde aktiviert
  "createdAt": "2026-05-01T10:00:00Z",
  "updatedAt": "2026-05-01T10:00:00Z",
  "updatedBy": "admin@example.com"
}
```

---

### 6️⃣ Verbindung testen

```bash
GET /api/admin/payment-config/{provider}/test
Authorization: Bearer {JWT_TOKEN}
```

**Response (erfolgreiche Verbindung):**
```json
{
  "success": true,
  "message": "Connection successful"
}
```

**Response (fehlerhafte Verbindung):**
```json
{
  "success": false,
  "message": "Connection failed"
}
```

---

### 7️⃣ Konfiguration löschen

```bash
DELETE /api/admin/payment-config/{provider}
Authorization: Bearer {JWT_TOKEN}
```

**Response:** `204 No Content`

---

## 🚀 Workflow: Live Konfiguration

### Schritt 1: Stripe API Keys speichern
```bash
curl -X POST http://localhost:8080/api/admin/payment-config/STRIPE \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "secretKey": "sk_test_51234567890",
    "publicKey": "pk_test_51234567890",
    "additionalConfig": null
  }'
```

### Schritt 2: Stripe aktivieren
```bash
curl -X POST http://localhost:8080/api/admin/payment-config/STRIPE/toggle \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Schritt 3: Verbindung testen
```bash
curl -X GET http://localhost:8080/api/admin/payment-config/STRIPE/test \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Schritt 4: Zahlung durchführen
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 29.99,
    "method": "STRIPE"
  }'
```

---

## 🔒 Sicherheit

### ⚠️ Wichtig:
- **Secret Keys werden NICHT** in den API Responses zurückgegeben
- **Nur ADMIN-Benutzer** können diese Endpoints verwenden
- **Alle Änderungen** werden mit Timestamp und Admin-Benutzer getracked
- **Secret Keys** werden verschlüsselt in der Datenbank gespeichert (empfohlen)

### Implementierung für verschlüsselte Speicherung:

Füge zu `PaymentConfig.java` hinzu:
```java
@Convert(converter = EncryptedStringConverter.class)
@Column(nullable = false)
private String secretKey;
```

---

## 🧪 Testen mit cURL

```bash
# 1. JWT Token abrufen (via Login)
JWT=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | jq -r '.accessToken')

# 2. Stripe konfigurieren
curl -X POST http://localhost:8080/api/admin/payment-config/STRIPE \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "secretKey": "sk_test_YOUR_KEY",
    "publicKey": "pk_test_YOUR_KEY"
  }'

# 3. Status prüfen
curl -X GET http://localhost:8080/api/admin/payment-config/STRIPE \
  -H "Authorization: Bearer $JWT"

# 4. Aktivieren
curl -X POST http://localhost:8080/api/admin/payment-config/STRIPE/toggle \
  -H "Authorization: Bearer $JWT"

# 5. Test
curl -X GET http://localhost:8080/api/admin/payment-config/STRIPE/test \
  -H "Authorization: Bearer $JWT"
```

---

## 📊 Datenbankschema

```sql
CREATE TABLE payment_configs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider VARCHAR(50) NOT NULL UNIQUE,
  secret_key VARCHAR(255) NOT NULL,
  public_key VARCHAR(255) NOT NULL,
  additional_config VARCHAR(255),
  enabled BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100)
);
```

---

## 🎨 Frontend UI Beispiel

```javascript
// Admin Payment Configuration Panel
async function configureStripe(secretKey, publicKey) {
  const token = localStorage.getItem('jwt_token');
  
  const response = await fetch('/api/admin/payment-config/STRIPE', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      secretKey: secretKey,
      publicKey: publicKey,
      additionalConfig: null
    })
  });
  
  if (response.ok) {
    console.log('✅ Stripe konfiguriert');
    // Aktivieren
    await fetch('/api/admin/payment-config/STRIPE/toggle', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }
}
```

---

## 🐛 Troubleshooting

| Problem | Lösung |
|---------|--------|
| 403 Forbidden | JWT Token überprüfen, ADMIN-Rolle benötigt |
| 404 Not Found | Provider-Name überprüfen (z.B. `STRIPE` nicht `stripe`) |
| 400 Bad Request | `secretKey` oder `publicKey` fehlt |
| 500 Internal Server Error | Logs checken, Datenbankverbindung prüfen |

