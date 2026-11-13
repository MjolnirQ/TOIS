package ru.nsu.tois.repository.suppliers;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.suppliers.SupplierProduct;
import ru.nsu.tois.entity.suppliers.SupplierProductId;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, SupplierProductId> {
}
