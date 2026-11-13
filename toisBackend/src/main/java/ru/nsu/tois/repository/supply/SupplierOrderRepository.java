package ru.nsu.tois.repository.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.supply.SupplierOrder;

import java.util.List;

public interface SupplierOrderRepository extends JpaRepository<SupplierOrder, Integer> {
    List<SupplierOrder> findBySupplyRequest_Id(Integer supplyRequestId);
}
