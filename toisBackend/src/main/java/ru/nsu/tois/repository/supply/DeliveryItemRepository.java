package ru.nsu.tois.repository.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.supply.DeliveryItem;

import java.util.List;
import java.util.Optional;

public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, Integer> {
    List<DeliveryItem> findByDelivery_Id(Integer deliveryId);

    @Query("select item.delivery.id from DeliveryItem item where item.id = :id")
    Optional<Integer> findDeliveryIdByItemId(@Param("id") Integer id);
}
