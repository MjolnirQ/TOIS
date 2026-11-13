package ru.nsu.tois.repository.suppliers;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.suppliers.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
}
