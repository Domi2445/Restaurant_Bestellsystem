# 🍕 Addons/Extras Management API

Diese Dokumentation beschreibt, wie Admins Addons/Extras (wie Soßen, Paprika, Extra Käse, etc.) zu Produkten hinzufügen können.

## 📋 Was sind Addons?

Addons sind optionale Extras, die Kunden zu ihrem Produkt hinzufügen können:
- **Soßen**: BBQ-Sauce, Knoblauch-Sauce, Chili-Sauce
- **Toppings**: Extra Paprika, Extra Käse, Pilze, Zwiebeln
- **Getränk-Add-ons**: Extra Eiswürfel, Zuckerfreie Alternative
- **Beliebige andere Extras**

Jedes Addon hat:
- **Name**: z.B. "Extra Mozzarella"
- **Beschreibung**: Optional, z.B. "Zusätzlicher Käse auf der Pizza"
- **Preis**: z.B. €1.50
- **MaxQuantity**: Maximale Anzahl dieses Addons pro Bestellung (z.B. 3)
- **Verfügbarkeit**: Kann aktiviert/deaktiviert werden

---

## 🔐 Authentifizierung

Alle Endpoints benötigen:
- **ADMIN-Rolle**
- **JWT Token**: `Authorization: Bearer {JWT_TOKEN}`

---

## 📋 API Endpoints - ADDONS

### 1️⃣ Alle Addons eines Produkts abrufen

```bash
GET /api/admin/menu/items/{itemId}/addons
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/menu/items/550e8400-e29b-41d4-a716-446655440001/addons
```

**Response:**
```json
[
  {
    "id": "addon-uuid-1",
    "name": "Extra Mozzarella",
    "description": "Zusätzlicher Käse auf der Pizza",
    "price": 1.50,
    "maxQuantity": 1,
    "available": true,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z"
  },
  {
    "id": "addon-uuid-2",
    "name": "BBQ-Sauce",
    "description": "Würzige BBQ-Sauce",
    "price": 0.50,
    "maxQuantity": 2,
    "available": true,
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z"
  }
]
```

---

### 2️⃣ Ein einzelnes Addon abrufen

```bash
GET /api/admin/menu/addons/{addonId}
Authorization: Bearer {JWT_TOKEN}

# Beispiel:
GET /api/admin/menu/addons/addon-uuid-1
```

**Response:** Wie oben (einzelnes Addon)

---

### 3️⃣ Neues Addon für ein Produkt erstellen

```bash
POST /api/admin/menu/items/{itemId}/addons
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "Extra Mozzarella",
  "description": "Zusätzlicher Käse auf der Pizza",
  "price": 1.50,
  "maxQuantity": 1,
  "available": true
}
```

**Response:** `201 Created` mit erstelltem Addon

---

### 4️⃣ Addon aktualisieren

```bash
PUT /api/admin/menu/addons/{addonId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "Extra Mozzarella - Premium",
  "description": "Premium Käse auf der Pizza",
  "price": 2.00,
  "maxQuantity": 2,
  "available": true
}
```

**Response:** `200 OK` mit aktualisiertem Addon

---

### 5️⃣ Addon löschen

```bash
DELETE /api/admin/menu/addons/{addonId}
Authorization: Bearer {JWT_TOKEN}
```

**Response:** `204 No Content`

---

### 6️⃣ Verfügbarkeit eines Addons ändern

```bash
POST /api/admin/menu/addons/{addonId}/toggle-availability
Authorization: Bearer {JWT_TOKEN}
```

**Response:** `200 OK` mit aktualisiertem Addon (available = true/false)

---

## 🧪 Komplettes Beispiel-Workflow: Pizza mit Addons erstellen

```bash
# 1. Admin-Token abrufen
JWT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.accessToken')

STORE_ID="550e8400-e29b-41d4-a716-446655440000"

# 2. Produkt erstellen
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

# 3. Größen hinzufügen
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/sizes \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"sizeName": "Small", "priceModifier": 0.00, "available": true}' | jq '.id' -r > /tmp/small_id

curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/sizes \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"sizeName": "Medium", "priceModifier": 2.50, "available": true}' | jq '.id' -r > /tmp/medium_id

curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/sizes \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"sizeName": "Large", "priceModifier": 5.00, "available": true}' | jq '.id' -r > /tmp/large_id

# 4. Addons hinzufügen
echo "Addons werden hinzugefügt..."

# Extra Mozzarella
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/addons \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Extra Mozzarella",
    "description": "Zusätzlicher frischer Mozzarella",
    "price": 1.50,
    "maxQuantity": 1,
    "available": true
  }' | jq .

# BBQ-Sauce
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/addons \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "BBQ-Sauce",
    "description": "Würzige BBQ-Sauce",
    "price": 0.50,
    "maxQuantity": 2,
    "available": true
  }' | jq .

# Paprika
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/addons \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Extra Paprika",
    "description": "Frische Paprikastücke",
    "price": 0.75,
    "maxQuantity": 2,
    "available": true
  }' | jq .

# Pilze
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/addons \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pilze",
    "description": "Frische Champignons",
    "price": 1.00,
    "maxQuantity": 3,
    "available": true
  }' | jq .

# Zwiebeln
curl -s -X POST http://localhost:8080/api/admin/menu/items/$ITEM_ID/addons \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zwiebeln",
    "description": "Rote und weiße Zwiebeln",
    "price": 0.50,
    "maxQuantity": 2,
    "available": true
  }' | jq .

# 5. Fertig! Das komplette Produkt mit Größen und Addons abrufen
echo "Finale Produktstruktur:"
curl -s -X GET http://localhost:8080/api/admin/menu/items/$ITEM_ID \
  -H "Authorization: Bearer $JWT" | jq .
```

---

## 📊 Komplette Pizza mit Größen und Addons

Die API gibt jetzt folgende Struktur zurück:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "storeId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Margarita Pizza",
  "description": "Klassische Pizza mit Tomaten und Mozzarella",
  "basePrice": 12.99,
  "type": "PIZZA",
  "available": true,
  "sizes": [
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
  ],
  "addons": [
    {
      "id": "addon-uuid-1",
      "name": "Extra Mozzarella",
      "description": "Zusätzlicher frischer Mozzarella",
      "price": 1.50,
      "maxQuantity": 1,
      "available": true,
      "createdAt": "2026-05-01T10:00:00Z",
      "updatedAt": "2026-05-01T10:00:00Z"
    },
    {
      "id": "addon-uuid-2",
      "name": "BBQ-Sauce",
      "description": "Würzige BBQ-Sauce",
      "price": 0.50,
      "maxQuantity": 2,
      "available": true,
      "createdAt": "2026-05-01T10:00:00Z",
      "updatedAt": "2026-05-01T10:00:00Z"
    },
    {
      "id": "addon-uuid-3",
      "name": "Extra Paprika",
      "description": "Frische Paprikastücke",
      "price": 0.75,
      "maxQuantity": 2,
      "available": true,
      "createdAt": "2026-05-01T10:00:00Z",
      "updatedAt": "2026-05-01T10:00:00Z"
    }
  ]
}
```

---

## 💾 Datenbankschema

```sql
CREATE TABLE menu_item_addons (
  id UUID PRIMARY KEY,
  menu_item_id UUID NOT NULL REFERENCES menu_items(id),
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  max_quantity INT DEFAULT 1,
  available BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

---

## 🎨 Frontend Integration Beispiel

```javascript
class AdminMenuManager {
  // ... vorherige Methoden ...

  // Addons hinzufügen
  async addAddon(itemId, name, description, price, maxQuantity = 1) {
    const response = await fetch(`${this.apiBase}/items/${itemId}/addons`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.jwtToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name,
        description,
        price: parseFloat(price),
        maxQuantity,
        available: true
      })
    });
    return response.json();
  }

  // Alle Addons abrufen
  async getAddons(itemId) {
    const response = await fetch(`${this.apiBase}/items/${itemId}/addons`, {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });
    return response.json();
  }

  // Addon aktualisieren
  async updateAddon(addonId, name, price) {
    const response = await fetch(`${this.apiBase}/addons/${addonId}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${this.jwtToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ name, price: parseFloat(price), available: true })
    });
    return response.json();
  }

  // Addon löschen
  async deleteAddon(addonId) {
    return fetch(`${this.apiBase}/addons/${addonId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });
  }
}

// Nutzung:
const manager = new AdminMenuManager(jwtToken, storeId);

// Pizza erstellen
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

// Addons hinzufügen
await manager.addAddon(pizza.id, 'Extra Mozzarella', 'Zusätzlicher Käse', 1.50, 1);
await manager.addAddon(pizza.id, 'BBQ-Sauce', 'Würzige Sauce', 0.50, 2);
await manager.addAddon(pizza.id, 'Paprika', 'Frische Paprika', 0.75, 2);
await manager.addAddon(pizza.id, 'Pilze', 'Champignons', 1.00, 3);
```

---

## 📊 Preisberechnung mit Addons

Der Gesamtpreis für eine Bestellung mit Addons berechnet sich wie folgt:

```
Gesamtpreis = (basePrice + priceModifierDerGröße) + (addon1Price × addon1Qty) + (addon2Price × addon2Qty) + ...
```

**Beispiel:**
```
Pizza Margarita:
  - basePrice: €12.99
  - Größe Medium: +€2.50
  - Extra Mozzarella (×1): +€1.50
  - BBQ-Sauce (×2): +€1.00
  ───────────────────────
  Gesamtpreis: €18.00
```

---

## 🐛 Häufige Fehler

| Problem | Lösung |
|---------|--------|
| 403 Forbidden | JWT Token überprüfen, ADMIN-Rolle benötigt |
| 404 Not Found | Produkt-ID oder Addon-ID überprüfen |
| 400 Bad Request | Name fehlt oder ist leer |
| Addon wird nicht gespeichert | Price oder maxQuantity überprüfen |

---

## 📖 Weitere Dokumentationen

- **Menü Admin API**: `ADMIN_MENU_API.md` (Produkte & Größen)
- **Payment Konfiguration**: `PAYMENT_CONFIG_API.md`
- **Zahlungen**: `PAYMENT_INTEGRATION_COMPLETE_GUIDE.md`

