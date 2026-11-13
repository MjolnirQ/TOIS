package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductTransferItemRequest(
        @NotNull
        Integer productTransferId,

        @NotNull
        Integer productId,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
