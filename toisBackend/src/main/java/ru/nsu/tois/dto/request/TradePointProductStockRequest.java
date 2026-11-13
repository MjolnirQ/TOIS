package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TradePointProductStockRequest(
        @NotNull
        Integer tradePointId,

        @NotNull
        Integer productId,

        @NotNull
        @Min(0)
        Integer quantity
) {
}
