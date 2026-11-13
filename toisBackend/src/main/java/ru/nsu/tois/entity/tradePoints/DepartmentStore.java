package ru.nsu.tois.entity.tradePoints;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "department_store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentStore {
    @Id
    @Column(name = "trade_point_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trade_point_id")
    @NotNull
    private TradePoint tradePoint;
}
