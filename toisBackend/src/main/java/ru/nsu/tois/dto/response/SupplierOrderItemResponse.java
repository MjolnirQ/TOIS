package ru.nsu.tois.dto.response;

import java.math.BigDecimal;

public record SupplierOrderItemResponse(
        Integer id,
        Integer supplierOrderId,
        Integer productId,
        Integer quantity,
        BigDecimal purchasePrice
) {
}
