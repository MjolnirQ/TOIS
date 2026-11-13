package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaleItemRequest(
        @NotNull
        Integer saleId,

        @NotNull
        Integer productId,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        @DecimalMin("0")
        BigDecimal unitPrice
) {
}
