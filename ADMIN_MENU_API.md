# 🍕 Admin API für Menü-Management

Diese API ermöglicht es Admin-Benutzern, Produkte (MenuItems) und deren Größen zu verwalten.

## 🔐 Authentifizierung & Autorisierung

Alle Endpoints benötigen:
- **ADMIN-Rolle** (User mit `Role.ADMIN`)
- **JWT Token** im Header: `Authorization: Bearer YOUR_JWT_TOKEN`

## 📋 API Endpoints - PRODUKTE

### 1️⃣ Alle Produkte einer Filiale abrufen

```bash
GET /api/admin/menu/items/store/{storeId}
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/menu/items/store/550e8400-e29b-41d4-a716-446655440000
```

**Response:**
```json
[
  {
    "id": "product-uuid",
    "storeId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Margarita Pizza",
    "description": "Klassische Pizza mit Tomaten und Mozzarella",
    "basePrice": 12.99,
    "type": "PIZZA",
    "available": true,
    "sizes": [
      {
        "id": "size-uuid",
        "sizeName": "Medium",
        "priceModifier": 2.50,
        "available": true,
        "createdAt": "2026-05-01T10:00:00Z",
        "updatedAt": "2026-05-01T10:00:00Z"
      }
    ]
  }
]
```

---

### 2️⃣ Ein einzelnes Produkt abrufen

```bash
GET /api/admin/menu/items/{itemId}
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/menu/items/550e8400-e29b-41d4-a716-446655440001
```

**Response:** Wie oben

---

### 3️⃣ Neues Produkt erstellen

```bash
POST /api/admin/menu/items/store/{storeId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "Margarita Pizza",
  "description": "Klassische Pizza mit Tomaten und Mozzarella",
  "basePrice": 12.99,
  "type": "PIZZA",
  "available": true
}
```

**Verfügbare Types:**
- `PIZZA`
- `DRINK`
- `DESSERT`

**Response:** `201 Created` mit erstelltem Produkt

---

### 4️⃣ Produkt aktualisieren

```bash
PUT /api/admin/menu/items/{itemId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "Margarita Pizza - Groß",
  "description": "Klassische große Pizza mit Tomaten und Mozzarella",
  "basePrice": 14.99,
  "type": "PIZZA",
  "available": true
}
```

**Response:** `200 OK` mit aktualisiertem Produkt

---

### 5️⃣ Produkt löschen

```bash
DELETE /api/admin/menu/items/{itemId}
Authorization: Bearer {JWT_TOKEN}
```

**Info:** Löscht auch alle Größen des Produkts automatisch!

**Response:** `204 No Content`

---

### 6️⃣ Verfügbarkeit eines Produkts ändern

```bash
POST /api/admin/menu/items/{itemId}/toggle-availability
Authorization: Bearer {JWT_TOKEN}
```

**Response:** `200 OK` mit aktualisiertem Produkt (available = true/false)

---

## 📋 API Endpoints - GRÖẞEN

### 7️⃣ Alle Größen eines Produkts abrufen

```bash
GET /api/admin/menu/items/{itemId}/sizes
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/menu/items/550e8400-e29b-41d4-a716-446655440001/sizes
```

**Response:**
```json
[
  {
    "id": "size-uuid-1",
    "sizeName": "Small",
    "priceModifier": 0.00,
    "available": true,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z"
  },
  {
    "id": "size-uuid-2",
    "sizeName": "Medium",
    "priceModifier": 2.50,
    "available": true,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z"
  },
  {
    "id": "size-uuid-3",
    "sizeName": "Large",
    "priceModifier": 5.00,
    "available": true,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z"
  }
]
```

---

### 8️⃣ Eine einzelne Größe abrufen

```bash
GET /api/admin/menu/sizes/{sizeId}
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/menu/sizes/size-uuid-2
```

**Response:** Wie oben (einzelnes Size-Objekt)

---

### 9️⃣ Neue Größe für ein Produkt erstellen

```bash
POST /api/admin/menu/items/{itemId}/sizes
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "sizeName": "Medium",
  "priceModifier": 2.50,
  "available": true
}
```

**Beispiel-Konfiguration für Pizza:**
```json
[
  {
    "sizeName": "Small",
    "priceModifier": 0.00,
    "available": true
  },
  {
    "sizeName": "Medium",
    "priceModifier": 2.50,
    "available": true
  },
  {
    "sizeName": "Large",
    "priceModifier": 5.00,
    "available": true
  }
]
```

**Response:** `201 Created` mit erstellter Größe

---

### 🔟 Größe aktualisieren

```bash
PUT /api/admin/menu/sizes/{sizeId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "sizeName": "Medium",
  "priceModifier": 3.00,
  "available": true
}
```

**Response:** `200 OK` mit aktualisierter Größe

---

### 1️⃣1️⃣ Größe löschen

```bash
DELETE /api/admin/menu/sizes/{sizeId}
Authorization: Bearer {JWT_TOKEN}
```

**Response:** `204 No Content`

---

### 1️⃣2️⃣ Verfügbarkeit einer Größe ändern

```bash
POST /api/admin/menu/sizes/{sizeId}/toggle-availability
Authorization: Bearer {JWT_TOKEN}
```

**Response:** `200 OK` mit aktualisierter Größe (available = true/false)

---

## 🧪 Komplettes Beispiel-Workflow

```bash
# 1. Admin-Token abrufen
JWT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.accessToken')

STORE_ID="550e8400-e29b-41d4-a716-446655440000"

# 2. Neues Produkt erstellen
PIZZA=$(curl -s -X POST http://localhost:8080/api/admin/menu/items/store/$STORE_ID \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Margarita Pizza",
    "description": "Klassische Pizza mit Tomaten und Mozzarella",
    "basePrice": 12.99,
    "type": "PIZZA",
    "available": true
  }')

ITEM_ID=$(echo $PIZZA | jq -r '.id')
echo "Produkt erstellt: $ITEM_ID"

# 3. Größe Small hinzufügen
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/sizes \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "sizeName": "Small",
    "priceModifier": 0.00,
    "available": true
  }' | jq .

# 4. Größe Medium hinzufügen
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/sizes \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "sizeName": "Medium",
    "priceModifier": 2.50,
    "available": true
  }' | jq .

# 5. Größe Large hinzufügen
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/sizes \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "sizeName": "Large",
    "priceModifier": 5.00,
    "available": true
  }' | jq .

# 6. Vollständiges Produkt mit allen Größen abrufen
curl -s -X GET http://localhost:8080/api/admin/menu/items/$ITEM_ID \
  -H "Authorization: Bearer $JWT" | jq .

# 7. Verfügbarkeit toggen
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/toggle-availability \
  -H "Authorization: Bearer $JWT" | jq .available

# 8. Alle Produkte der Filiale abrufen
curl -s -X GET http://localhost:8080/api/admin/menu/items/store/$STORE_ID \
  -H "Authorization: Bearer $JWT" | jq .
```

---

## 💾 Datenbankschema

### menu_items
```sql
CREATE TABLE menu_items (
  id UUID PRIMARY KEY,
  store_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  base_price DECIMAL(10,2) NOT NULL,
  type VARCHAR(50) NOT NULL,
  available BOOLEAN DEFAULT TRUE
);
```

### menu_item_sizes (NEU)
```sql
CREATE TABLE menu_item_sizes (
  id UUID PRIMARY KEY,
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  size_name VARCHAR(50) NOT NULL,
  price_modifier DECIMAL(10,2),
  available BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

---

## 🎨 Frontend Integration Beispiel

```javascript
class AdminMenuManager {
  constructor(jwtToken, storeId) {
    this.jwtToken = jwtToken;
    this.storeId = storeId;
    this.apiBase = '/api/admin/menu';
  }

  // Neues Produkt erstellen
  async createProduct(name, description, basePrice, type) {
    const response = await fetch(`${this.apiBase}/items/store/${this.storeId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.jwtToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name, description, basePrice,
        type: type.toUpperCase(),
        available: true
      })
    });
    return response.json();
  }

  // Größen hinzufügen
  async addSize(itemId, sizeName, priceModifier) {
    const response = await fetch(`${this.apiBase}/items/${itemId}/sizes`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.jwtToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        sizeName,
        priceModifier: parseFloat(priceModifier),
        available: true
      })
    });
    return response.json();
  }

  // Alle Produkte abrufen
  async getProducts() {
    const response = await fetch(
      `${this.apiBase}/items/store/${this.storeId}`,
      { headers: { 'Authorization': `Bearer ${this.jwtToken}` } }
    );
    return response.json();
  }
}

// Nutzung:
const manager = new AdminMenuManager(jwtToken, storeId);

// Produkt erstellen
const pizza = await manager.createProduct(
  'Margarita Pizza',
  'Klassische Pizza mit Tomaten und Mozzarella',
  12.99,
  'pizza'
);

// Größen hinzufügen
await manager.addSize(pizza.id, 'Small', 0.00);
await manager.addSize(pizza.id, 'Medium', 2.50);
await manager.addSize(pizza.id, 'Large', 5.00);
```

---

## 🐛 Häufige Fehler

| Problem | Lösung |
|---------|--------|
| 403 Forbidden | JWT Token überprüfen, ADMIN-Rolle benötigt |
| 404 Not Found | UUID überprüfen (itemId, storeId, sizeId) |
| 400 Bad Request | Name oder basePrice fehlt / Ungültiger Type |
| Produkt hat keine Größen | Neue Größen via POST hinzufügen |

---

## 📊 Preisberechnung

Der tatsächliche Preis für eine Größe berechnet sich wie folgt:

```
Finaler Preis = basePrice + priceModifier
```

**Beispiel:**
```
Pizza Margarita:
  - basePrice: €12.99
  
  Small:  €12.99 + €0.00 = €12.99
  Medium: €12.99 + €2.50 = €15.49
  Large:  €12.99 + €5.00 = €17.99
```

---

## 📖 Weitere Informationen

- **Menü abrufen (öffentlich)**: `GET /api/menu/store/{storeId}`
- **Menü aktualisieren (Admin)**: Diese API
- **Zahlungskonfiguration**: `/api/admin/payment-config`

