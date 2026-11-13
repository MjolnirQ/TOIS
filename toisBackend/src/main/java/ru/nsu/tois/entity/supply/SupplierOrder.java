package ru.nsu.tois.entity.supply;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.nsu.tois.entity.employees.TradeManager;
import ru.nsu.tois.entity.suppliers.Supplier;

import java.time.LocalDate;

@Entity
@Table(name = "supplier_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_date")
    @NotNull
    private LocalDate orderDate;

    @Column(name = "order_number")
    @NotBlank
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @NotNull
    private TradeManager manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @NotNull
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_request_id")
    @NotNull
    private SupplyRequest supplyRequest;

    @NotBlank
    private String status;
}
