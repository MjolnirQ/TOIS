package ru.nsu.tois.repository.catalog;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.catalog.TradePointProductStock;
import ru.nsu.tois.entity.catalog.TradePointProductStockId;

import java.util.List;
import java.util.Optional;

public interface TradePointProductStockRepository extends JpaRepository<TradePointProductStock, TradePointProductStockId> {
    List<TradePointProductStock> findByIdTradePointId(Integer tradePointId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from TradePointProductStock stock where stock.id = :id")
    Optional<TradePointProductStock> findByIdForUpdate(@Param("id") TradePointProductStockId id);
}
