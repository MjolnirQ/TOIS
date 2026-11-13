package ru.nsu.tois.repository.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.supply.SupplyRequestItem;

public interface SupplyRequestItemRepository extends JpaRepository<SupplyRequestItem, Integer> {
}
