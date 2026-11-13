package ru.nsu.tois.entity.supply;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.catalog.Product;

@Entity
@Table(name = "supply_request_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_request_id")
    @NotNull
    private SupplyRequest supplyRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product;

    @Column(name = "requested_quantity")
    @NotNull
    @Min(1)
    private Integer requestedQuantity;
}
