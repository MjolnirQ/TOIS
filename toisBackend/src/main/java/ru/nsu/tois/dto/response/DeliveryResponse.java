package ru.nsu.tois.dto.response;

import java.time.LocalDate;

public record DeliveryResponse(
        Integer id,
        Integer supplierId,
        Integer supplierOrderId,
        LocalDate deliveryDate
) {
}
