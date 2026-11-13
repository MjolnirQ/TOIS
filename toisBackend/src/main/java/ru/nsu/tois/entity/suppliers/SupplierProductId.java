package ru.nsu.tois.entity.suppliers;

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
public class SupplierProductId implements Serializable {
    @Column(name = "supplier_id")
    @NotNull
    private Integer supplierId;

    @Column(name = "product_id")
    @NotNull
    private Integer productId;
}
