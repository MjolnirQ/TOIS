package ru.nsu.tois.entity.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.tradePoints.TradePoint;

import java.math.BigDecimal;

@Entity
@Table(name = "trade_point_product_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradePointProductPrice {
    @EmbeddedId
    @NotNull
    private TradePointProductPriceId id;

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
    @DecimalMin("0")
    private BigDecimal price;
}
