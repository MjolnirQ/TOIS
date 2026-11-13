package ru.nsu.tois.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.catalog.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findFirstByNameIgnoreCase(String name);
}
