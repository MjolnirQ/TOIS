package ru.nsu.tois.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DeliveryRequest(
        @NotNull
        Integer supplierId,

        @NotNull
        Integer supplierOrderId,

        @NotNull
        LocalDate deliveryDate
) {
}
