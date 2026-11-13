package ru.nsu.tois.entity.tradePoints;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "stall")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stall {
    @Id
    @Column(name = "trade_point_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trade_point_id")
    @NotNull
    private TradePoint tradePoint;
}
