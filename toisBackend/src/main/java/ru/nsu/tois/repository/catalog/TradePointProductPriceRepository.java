package ru.nsu.tois.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.catalog.TradePointProductPrice;
import ru.nsu.tois.entity.catalog.TradePointProductPriceId;

import java.util.List;

public interface TradePointProductPriceRepository extends JpaRepository<TradePointProductPrice, TradePointProductPriceId> {
    List<TradePointProductPrice> findByIdTradePointId(Integer tradePointId);
}
