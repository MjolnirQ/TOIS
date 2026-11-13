package ru.nsu.tois.entity.assignments;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.employees.Seller;
import ru.nsu.tois.entity.tradePoints.Hall;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Entity
@Table(name = "seller_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    @NotNull
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_point_id")
    @NotNull
    private TradePoint tradePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id")
    private Hall hall;
}
