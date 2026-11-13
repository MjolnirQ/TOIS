package ru.nsu.tois.repository.assignments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.tois.entity.assignments.SellerAssignment;
import ru.nsu.tois.entity.tradePoints.TradePoint;

import java.util.List;

public interface SellerAssignmentRepository extends JpaRepository<SellerAssignment, Integer> {
    boolean existsBySeller_IdAndTradePoint_Id(Integer sellerId, Integer tradePointId);

    List<SellerAssignment> findBySeller_Id(Integer sellerId);

    @Query("""
            select sa.tradePoint
            from SellerAssignment sa
            where sa.seller.id = :sellerId
            order by sa.tradePoint.name
            """)
    List<TradePoint> findAssignedTradePoints(@Param("sellerId") Integer sellerId);
}
