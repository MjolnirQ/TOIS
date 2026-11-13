package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PriceUpdateRequest(
        @NotNull
        @DecimalMin("0")
        BigDecimal price
) {
}
