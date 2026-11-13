package org.example.http_client.DTOresponse;

import java.math.BigDecimal;

public record SaleItemResponse(
        Integer id,
        Integer saleId,
        Integer productId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
