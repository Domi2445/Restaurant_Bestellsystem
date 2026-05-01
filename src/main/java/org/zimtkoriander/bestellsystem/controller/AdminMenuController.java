package org.zimtkoriander.bestellsystem.controller;

import Model.MenuItem;
import Model.MenuItemSize;
import Model.MenuItemAddon;
import Model.MenuItemType;
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
import org.zimtkoriander.bestellsystem.dto.MenuItemCreateRequest;
import org.zimtkoriander.bestellsystem.dto.MenuItemDetailedResponse;
import org.zimtkoriander.bestellsystem.dto.MenuItemSizeRequest;
import org.zimtkoriander.bestellsystem.dto.MenuItemSizeResponse;
import org.zimtkoriander.bestellsystem.dto.MenuItemAddonRequest;
import org.zimtkoriander.bestellsystem.dto.MenuItemAddonResponse;
import org.zimtkoriander.bestellsystem.repository.MenuItemRepository;
import org.zimtkoriander.bestellsystem.repository.MenuItemSizeRepository;
import org.zimtkoriander.bestellsystem.repository.MenuItemAddonRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin API für Menü-Management (Produkte und Größen)
 * Benötigt ADMIN-Rolle für Zugriff
 */
@RestController
@RequestMapping("/api/admin/menu")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuController {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemSizeRepository menuItemSizeRepository;
    private final MenuItemAddonRepository menuItemAddonRepository;

    public AdminMenuController(MenuItemRepository menuItemRepository,
                             MenuItemSizeRepository menuItemSizeRepository,
                             MenuItemAddonRepository menuItemAddonRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuItemSizeRepository = menuItemSizeRepository;
        this.menuItemAddonRepository = menuItemAddonRepository;
    }

    // =====================================================================
    // MENÜ ITEMS (PRODUKTE)
    // =====================================================================

    /**
     * Abrufen aller Produkte einer Filiale
     */
    @GetMapping("/items/store/{storeId}")
    public ResponseEntity<List<MenuItemDetailedResponse>> getMenuItemsByStore(@PathVariable UUID storeId) {
        List<MenuItemDetailedResponse> items = menuItemRepository.findByStoreId(storeId)
                .stream()
                .map(this::mapMenuItemToDetailedResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    /**
     * Abrufen eines spezifischen Produkts mit allen Größen
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<MenuItemDetailedResponse> getMenuItem(@PathVariable UUID itemId) {
        return menuItemRepository.findById(itemId)
                .map(item -> ResponseEntity.ok(mapMenuItemToDetailedResponse(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Neues Produkt erstellen
     *
     * Beispiel-Request:
     * {
     *   "name": "Margarita Pizza",
     *   "description": "Klassische Pizza mit Tomaten und Mozzarella",
     *   "basePrice": 12.99,
     *   "type": "PIZZA",
     *   "available": true
     * }
     */
    @PostMapping("/items/store/{storeId}")
    public ResponseEntity<MenuItemDetailedResponse> createMenuItem(
            @PathVariable UUID storeId,
            @RequestBody MenuItemCreateRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MenuItem item = new MenuItem();
            item.setStoreId(storeId);
            item.setName(request.getName());
            item.setDescription(request.getDescription());
            item.setBasePrice(request.getBasePrice());
            item.setType(MenuItemType.valueOf(request.getType().toUpperCase()));
            item.setAvailable(request.isAvailable());

            MenuItem saved = menuItemRepository.save(item);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapMenuItemToDetailedResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Produkt aktualisieren
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<MenuItemDetailedResponse> updateMenuItem(
            @PathVariable UUID itemId,
            @RequestBody MenuItemCreateRequest request) {

        return menuItemRepository.findById(itemId)
                .map(item -> {
                    if (request.getName() != null && !request.getName().isBlank()) {
                        item.setName(request.getName());
                    }
                    if (request.getDescription() != null) {
                        item.setDescription(request.getDescription());
                    }
                    if (request.getBasePrice() != null) {
                        item.setBasePrice(request.getBasePrice());
                    }
                    if (request.getType() != null) {
                        try {
                            item.setType(MenuItemType.valueOf(request.getType().toUpperCase()));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    item.setAvailable(request.isAvailable());

                    MenuItem updated = menuItemRepository.save(item);
                    return ResponseEntity.ok(mapMenuItemToDetailedResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Produkt löschen
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable UUID itemId) {
        if (!menuItemRepository.existsById(itemId)) {
            return ResponseEntity.notFound().build();
        }

        // Lösche auch alle Größen dieses Produkts
        List<MenuItemSize> sizes = menuItemSizeRepository.findByMenuItemId(itemId);
        menuItemSizeRepository.deleteAll(sizes);

        // Lösche auch alle Addons dieses Produkts
        List<MenuItemAddon> addons = menuItemAddonRepository.findByMenuItemId(itemId);
        menuItemAddonRepository.deleteAll(addons);

        menuItemRepository.deleteById(itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verfügbarkeit eines Produkts ändern
     */
    @PostMapping("/items/{itemId}/toggle-availability")
    public ResponseEntity<MenuItemDetailedResponse> toggleMenuItemAvailability(
            @PathVariable UUID itemId) {

        return menuItemRepository.findById(itemId)
                .map(item -> {
                    item.setAvailable(!item.isAvailable());
                    MenuItem updated = menuItemRepository.save(item);
                    return ResponseEntity.ok(mapMenuItemToDetailedResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // MENU ITEM ADDONS (EXTRAS/TOPPINGS)
    // =====================================================================

    /**
     * Alle Addons eines Produkts abrufen
     */
    @GetMapping("/items/{itemId}/addons")
    public ResponseEntity<List<MenuItemAddonResponse>> getMenuItemAddons(@PathVariable UUID itemId) {
        if (!menuItemRepository.existsById(itemId)) {
            return ResponseEntity.notFound().build();
        }

        List<MenuItemAddonResponse> addons = menuItemAddonRepository.findByMenuItemId(itemId)
                .stream()
                .map(this::mapAddonToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(addons);
    }

    /**
     * Ein einzelnes Addon abrufen
     */
    @GetMapping("/addons/{addonId}")
    public ResponseEntity<MenuItemAddonResponse> getMenuItemAddon(@PathVariable UUID addonId) {
        return menuItemAddonRepository.findById(addonId)
                .map(addon -> ResponseEntity.ok(mapAddonToResponse(addon)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Neues Addon für ein Produkt erstellen
     *
     * Beispiel-Request:
     * {
     *   "name": "Extra Mozzarella",
     *   "description": "Zusätzlicher Käse auf der Pizza",
     *   "price": 1.50,
     *   "maxQuantity": 1,
     *   "available": true
     * }
     */
    @PostMapping("/items/{itemId}/addons")
    public ResponseEntity<MenuItemAddonResponse> createMenuItemAddon(
            @PathVariable UUID itemId,
            @RequestBody MenuItemAddonRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return menuItemRepository.findById(itemId)
                .map(item -> {
                    MenuItemAddon addon = new MenuItemAddon();
                    addon.setMenuItem(item);
                    addon.setName(request.getName());
                    addon.setDescription(request.getDescription());
                    addon.setPrice(request.getPrice() != null ?
                            request.getPrice() : java.math.BigDecimal.ZERO);
                    addon.setMaxQuantity(request.getMaxQuantity() != null ?
                            request.getMaxQuantity() : 1);
                    addon.setAvailable(request.isAvailable());

                    MenuItemAddon saved = menuItemAddonRepository.save(addon);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(mapAddonToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Addon aktualisieren
     */
    @PutMapping("/addons/{addonId}")
    public ResponseEntity<MenuItemAddonResponse> updateMenuItemAddon(
            @PathVariable UUID addonId,
            @RequestBody MenuItemAddonRequest request) {

        return menuItemAddonRepository.findById(addonId)
                .map(addon -> {
                    if (request.getName() != null && !request.getName().isBlank()) {
                        addon.setName(request.getName());
                    }
                    if (request.getDescription() != null) {
                        addon.setDescription(request.getDescription());
                    }
                    if (request.getPrice() != null) {
                        addon.setPrice(request.getPrice());
                    }
                    if (request.getMaxQuantity() != null) {
                        addon.setMaxQuantity(request.getMaxQuantity());
                    }
                    addon.setAvailable(request.isAvailable());

                    MenuItemAddon updated = menuItemAddonRepository.save(addon);
                    return ResponseEntity.ok(mapAddonToResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Addon löschen
     */
    @DeleteMapping("/addons/{addonId}")
    public ResponseEntity<Void> deleteMenuItemAddon(@PathVariable UUID addonId) {
        if (!menuItemAddonRepository.existsById(addonId)) {
            return ResponseEntity.notFound().build();
        }

        menuItemAddonRepository.deleteById(addonId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verfügbarkeit eines Addons ändern
     */
    @PostMapping("/addons/{addonId}/toggle-availability")
    public ResponseEntity<MenuItemAddonResponse> toggleAddonAvailability(
            @PathVariable UUID addonId) {

        return menuItemAddonRepository.findById(addonId)
                .map(addon -> {
                    addon.setAvailable(!addon.isAvailable());
                    MenuItemAddon updated = menuItemAddonRepository.save(addon);
                    return ResponseEntity.ok(mapAddonToResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // MENU ITEM SIZES (GRÖẞEN)
    // =====================================================================

    /**
     * Alle Größen eines Produkts abrufen
     */
    @GetMapping("/items/{itemId}/sizes")
    public ResponseEntity<List<MenuItemSizeResponse>> getMenuItemSizes(@PathVariable UUID itemId) {
        if (!menuItemRepository.existsById(itemId)) {
            return ResponseEntity.notFound().build();
        }

        List<MenuItemSizeResponse> sizes = menuItemSizeRepository.findByMenuItemId(itemId)
                .stream()
                .map(this::mapSizeToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sizes);
    }

    /**
     * Eine einzelne Größe abrufen
     */
    @GetMapping("/sizes/{sizeId}")
    public ResponseEntity<MenuItemSizeResponse> getMenuItemSize(@PathVariable UUID sizeId) {
        return menuItemSizeRepository.findById(sizeId)
                .map(size -> ResponseEntity.ok(mapSizeToResponse(size)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Neue Größe für ein Produkt erstellen
     *
     * Beispiel-Request:
     * {
     *   "sizeName": "Medium",
     *   "priceModifier": 2.50,
     *   "available": true
     * }
     */
    @PostMapping("/items/{itemId}/sizes")
    public ResponseEntity<MenuItemSizeResponse> createMenuItemSize(
            @PathVariable UUID itemId,
            @RequestBody MenuItemSizeRequest request) {

        if (request.getSizeName() == null || request.getSizeName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return menuItemRepository.findById(itemId)
                .map(item -> {
                    MenuItemSize size = new MenuItemSize();
                    size.setMenuItem(item);
                    size.setSizeName(request.getSizeName());
                    size.setPriceModifier(request.getPriceModifier() != null ?
                            request.getPriceModifier() : java.math.BigDecimal.ZERO);
                    size.setAvailable(request.isAvailable());

                    MenuItemSize saved = menuItemSizeRepository.save(size);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(mapSizeToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Größe aktualisieren
     */
    @PutMapping("/sizes/{sizeId}")
    public ResponseEntity<MenuItemSizeResponse> updateMenuItemSize(
            @PathVariable UUID sizeId,
            @RequestBody MenuItemSizeRequest request) {

        return menuItemSizeRepository.findById(sizeId)
                .map(size -> {
                    if (request.getSizeName() != null && !request.getSizeName().isBlank()) {
                        size.setSizeName(request.getSizeName());
                    }
                    if (request.getPriceModifier() != null) {
                        size.setPriceModifier(request.getPriceModifier());
                    }
                    size.setAvailable(request.isAvailable());

                    MenuItemSize updated = menuItemSizeRepository.save(size);
                    return ResponseEntity.ok(mapSizeToResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Größe löschen
     */
    @DeleteMapping("/sizes/{sizeId}")
    public ResponseEntity<Void> deleteMenuItemSize(@PathVariable UUID sizeId) {
        if (!menuItemSizeRepository.existsById(sizeId)) {
            return ResponseEntity.notFound().build();
        }

        menuItemSizeRepository.deleteById(sizeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verfügbarkeit einer Größe ändern
     */
    @PostMapping("/sizes/{sizeId}/toggle-availability")
    public ResponseEntity<MenuItemSizeResponse> toggleSizeAvailability(
            @PathVariable UUID sizeId) {

        return menuItemSizeRepository.findById(sizeId)
                .map(size -> {
                    size.setAvailable(!size.isAvailable());
                    MenuItemSize updated = menuItemSizeRepository.save(size);
                    return ResponseEntity.ok(mapSizeToResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Hilfsmethoden
    // =====================================================================

    private MenuItemDetailedResponse mapMenuItemToDetailedResponse(MenuItem item) {
        List<MenuItemSizeResponse> sizes = menuItemSizeRepository.findByMenuItemId(item.getId())
                .stream()
                .map(this::mapSizeToResponse)
                .collect(Collectors.toList());

        List<MenuItemAddonResponse> addons = menuItemAddonRepository.findByMenuItemId(item.getId())
                .stream()
                .map(this::mapAddonToResponse)
                .collect(Collectors.toList());

        return new MenuItemDetailedResponse(
                item.getId(),
                item.getStoreId(),
                item.getName(),
                item.getDescription(),
                item.getBasePrice(),
                item.getType().name(),
                item.isAvailable(),
                sizes,
                addons
        );
    }

    private MenuItemSizeResponse mapSizeToResponse(MenuItemSize size) {
        return new MenuItemSizeResponse(
                size.getId(),
                size.getSizeName(),
                size.getPriceModifier(),
                size.isAvailable(),
                size.getCreatedAt(),
                size.getUpdatedAt()
        );
    }

    private MenuItemAddonResponse mapAddonToResponse(MenuItemAddon addon) {
        return new MenuItemAddonResponse(
                addon.getId(),
                addon.getName(),
                addon.getDescription(),
                addon.getPrice(),
                addon.getMaxQuantity(),
                addon.isAvailable(),
                addon.getCreatedAt(),
                addon.getUpdatedAt()
        );
    }
}

