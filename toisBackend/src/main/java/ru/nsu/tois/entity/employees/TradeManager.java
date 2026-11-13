package ru.nsu.tois.entity.employees;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "trade_manager")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeManager {
    @Id
    @Column(name = "employee_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "employee_id")
    @NotNull
    private Employee employee;
}
