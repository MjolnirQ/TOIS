package ru.nsu.tois.repository.supply;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.nsu.tois.entity.supply.Delivery;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findBySupplierOrder_Id(Integer supplierOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Delivery> findLockedById(Integer id);
}
