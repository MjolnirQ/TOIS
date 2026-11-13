package ru.nsu.tois.entity.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TradePointProductPriceId implements Serializable {
    @Column(name = "trade_point_id")
    @NotNull
    private Integer tradePointId;

    @Column(name = "product_id")
    @NotNull
    private Integer productId;
}
