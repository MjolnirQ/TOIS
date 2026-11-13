package ru.nsu.tois.dto.response;

import java.math.BigDecimal;

public record SupplierProductResponse(
        Integer supplierId,
        Integer productId,
        BigDecimal supplierPrice
) {
}
