package org.zimtkoriander.bestellsystem.repository;

import Model.MenuItemSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemSizeRepository extends JpaRepository<MenuItemSize, UUID> {
    List<MenuItemSize> findByMenuItemId(UUID menuItemId);
}

