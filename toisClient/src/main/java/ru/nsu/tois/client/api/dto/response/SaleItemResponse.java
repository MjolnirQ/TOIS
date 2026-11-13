package ru.nsu.tois.client.api.dto.response;

import java.math.BigDecimal;

public record SaleItemResponse(
        Integer id,
        Integer saleId,
        Integer productId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
