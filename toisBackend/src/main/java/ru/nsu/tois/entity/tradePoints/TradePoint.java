package ru.nsu.tois.entity.tradePoints;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "trade_point")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "trade_point_type")
    @NotNull
    private TradePointType type;

    @Column(name = "trade_area")
    @DecimalMin("0")
    private BigDecimal tradeArea;

    @Column(name = "utility_payments")
    @DecimalMin("0")
    private BigDecimal utilityPayments;

    @Column(name = "counter_count")
    @Min(0)
    private Integer counterCount;

    @Column(name = "rent_payment")
    @DecimalMin("0")
    private BigDecimal rentPayment;
}
