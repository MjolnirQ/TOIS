package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SupplyRequestItemRequest(
        @NotNull
        Integer supplyRequestId,

        @NotNull
        Integer productId,

        @NotNull
        @Min(1)
        Integer requestedQuantity
) {
}
