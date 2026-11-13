package ru.nsu.tois.entity.transfers;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.tradePoints.TradePoint;

import java.time.LocalDate;

@Entity
@Table(name = "product_transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transfer_date")
    @NotNull
    private LocalDate transferDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_trade_point_id")
    @NotNull
    private TradePoint senderTradePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_trade_point_id")
    @NotNull
    private TradePoint receiverTradePoint;
}
