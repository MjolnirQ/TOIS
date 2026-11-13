package ru.nsu.tois.entity.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.tradePoints.TradePoint;

@Entity
@Table(name = "trade_point_product_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradePointProductStock {
    @EmbeddedId
    @NotNull
    private TradePointProductStockId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tradePointId")
    @JoinColumn(name = "trade_point_id")
    @NotNull
    private TradePoint tradePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product;

    @NotNull
    @Min(0)
    private Integer quantity;
}
