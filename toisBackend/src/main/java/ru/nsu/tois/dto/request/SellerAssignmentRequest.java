package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

public record SellerAssignmentRequest(
        @NotNull
        Integer sellerId,

        @NotNull
        Integer tradePointId,

        Integer hallId
) {
}
