package ru.nsu.tois.entity.supply;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.catalog.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    @NotNull
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product;

    @NotNull
    @Min(1)
    private Integer quantity;

    @Column(name = "delivery_price")
    @NotNull
    @DecimalMin("0")
    private BigDecimal deliveryPrice;
}
