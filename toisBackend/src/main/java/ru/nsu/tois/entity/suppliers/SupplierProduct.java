package ru.nsu.tois.entity.suppliers;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.catalog.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProduct {
    @EmbeddedId
    @NotNull
    private SupplierProductId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("supplierId")
    @JoinColumn(name = "supplier_id")
    @NotNull
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product;

    @Column(name = "supplier_price")
    @DecimalMin("0")
    private BigDecimal supplierPrice;
}
