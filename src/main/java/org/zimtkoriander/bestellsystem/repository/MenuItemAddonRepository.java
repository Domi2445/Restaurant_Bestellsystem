package org.zimtkoriander.bestellsystem.repository;

import Model.MenuItemAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemAddonRepository extends JpaRepository<MenuItemAddon, UUID> {
    List<MenuItemAddon> findByMenuItemId(UUID menuItemId);
}

