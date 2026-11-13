package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SupplyRequestRequest(
        @NotNull
        LocalDate requestDate,

        @NotNull
        Integer tradePointId,

        @NotBlank
        String status
) {
}
