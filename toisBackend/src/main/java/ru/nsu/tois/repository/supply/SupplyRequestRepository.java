package ru.nsu.tois.repository.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.supply.SupplyRequest;

public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, Integer> {
}
