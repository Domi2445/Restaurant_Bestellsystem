package org.zimtkoriander.bestellsystem.controller;

import Model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.dto.MenuItemResponse;
import org.zimtkoriander.bestellsystem.repository.MenuItemRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<MenuItemResponse>> getMenuByStore(@PathVariable UUID storeId) {
        List<MenuItemResponse> items = menuItemRepository.findByStoreId(storeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        List<MenuItemResponse> items = menuItemRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    private MenuItemResponse mapToResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getStoreId(),
                item.getName(),
                item.getDescription(),
                item.getBasePrice(),
                item.getType().name(),
                item.isAvailable()
        );
    }
}

