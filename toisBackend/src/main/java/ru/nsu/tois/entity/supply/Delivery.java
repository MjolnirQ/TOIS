package ru.nsu.tois.entity.supply;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.suppliers.Supplier;

import java.time.LocalDate;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @NotNull
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_order_id")
    @NotNull
    private SupplierOrder supplierOrder;

    @Column(name = "delivery_date")
    @NotNull
    private LocalDate deliveryDate;
}
