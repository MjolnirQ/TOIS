package ru.nsu.tois.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.entity.catalog.Product;
import ru.nsu.tois.repository.catalog.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductResolverService {
    private final ProductRepository productRepository;

    @Transactional
    public Product requireOrCreateProduct(Integer productId, String productName, String units) {
        if (productId != null) {
            return productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        }

        String normalizedName = normalize(productName);
        if (normalizedName == null) {
            throw new IllegalArgumentException("Either productId or productName must be provided");
        }

        return productRepository.findFirstByNameIgnoreCase(normalizedName)
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name(normalizedName)
                        .units(normalize(units))
                        .build()));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
