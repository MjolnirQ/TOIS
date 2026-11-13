package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HallRequest(
        @NotNull
        Integer tradePointId,

        Integer floor,

        @DecimalMin("0")
        BigDecimal area
) {
}
