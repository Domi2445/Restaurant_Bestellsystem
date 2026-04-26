package org.zimtkoriander.bestellsystem.repository;

import Model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByZipCodeAndCity(String zipCode, String city);
}

