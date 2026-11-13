package ru.nsu.tois.repository.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.supply.SupplierOrderItem;

public interface SupplierOrderItemRepository extends JpaRepository<SupplierOrderItem, Integer> {
}
