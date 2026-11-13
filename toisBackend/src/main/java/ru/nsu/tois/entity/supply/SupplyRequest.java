package ru.nsu.tois.entity.supply;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.tradePoints.TradePoint;

import java.time.LocalDate;

@Entity
@Table(name = "supply_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_date")
    @NotNull
    private LocalDate requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_point_id")
    @NotNull
    private TradePoint tradePoint;

    @NotBlank
    private String status;
}
