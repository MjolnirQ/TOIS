package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SupplierOrderRequest(
        @NotNull
        LocalDate orderDate,

        @NotBlank
        String orderNumber,

        @NotNull
        Integer managerId,

        @NotNull
        Integer supplierId,

        @NotNull
        Integer supplyRequestId,

        @NotBlank
        String status
) {
}
