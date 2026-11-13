package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.nsu.tois.entity.tradePoints.TradePointType;

import java.math.BigDecimal;

public record TradePointRequest(
        @NotBlank
        String name,

        @NotBlank
        String address,

        @NotNull
        TradePointType type,

        @DecimalMin("0")
        BigDecimal tradeArea,

        @DecimalMin("0")
        BigDecimal utilityPayments,

        @Min(0)
        Integer counterCount,

        @DecimalMin("0")
        BigDecimal rentPayment
) {
}
