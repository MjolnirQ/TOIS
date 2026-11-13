package ru.nsu.tois.repository.transfers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.transfers.ProductTransferItem;

import java.util.List;
import java.util.Optional;

public interface ProductTransferItemRepository extends JpaRepository<ProductTransferItem, Integer> {
    List<ProductTransferItem> findByProductTransfer_Id(Integer productTransferId);

    @Query("select item.productTransfer.id from ProductTransferItem item where item.id = :id")
    Optional<Integer> findTransferIdByItemId(@Param("id") Integer id);
}
