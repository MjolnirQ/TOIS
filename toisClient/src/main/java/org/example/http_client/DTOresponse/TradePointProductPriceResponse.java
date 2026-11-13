package org.example.http_client.DTOresponse;

import java.math.BigDecimal;

public record TradePointProductPriceResponse(
        Integer tradePointId,
        Integer productId,
        BigDecimal price
) {
}
