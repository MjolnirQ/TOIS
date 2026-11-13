package ru.nsu.tois.repository.tradePoints;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.tois.entity.tradePoints.TradePoint;

public interface TradePointRepository extends JpaRepository<TradePoint, Integer> {
}
