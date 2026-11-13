package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SaleRequest(
        @NotNull
        LocalDate saleDate,

        @NotNull
        Integer tradePointId,

        @NotNull
        Integer sellerId,

        Integer customerId
) {
}
