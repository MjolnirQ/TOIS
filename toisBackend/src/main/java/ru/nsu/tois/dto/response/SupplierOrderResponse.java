package ru.nsu.tois.dto.response;

import java.time.LocalDate;

public record SupplierOrderResponse(
        Integer id,
        LocalDate orderDate,
        String orderNumber,
        Integer managerId,
        Integer supplierId,
        Integer supplyRequestId,
        String status
) {
}
