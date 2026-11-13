package ru.nsu.tois.repository.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.sales.SaleItem;

import java.util.List;
import java.util.Optional;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
    List<SaleItem> findBySale_Id(Integer saleId);

    @Query("select item.sale.id from SaleItem item where item.id = :id")
    Optional<Integer> findSaleIdByItemId(@Param("id") Integer id);
}
