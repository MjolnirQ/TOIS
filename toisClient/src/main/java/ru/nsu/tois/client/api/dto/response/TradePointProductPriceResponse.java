package ru.nsu.tois.client.api.dto.response;

import java.math.BigDecimal;

public record TradePointProductPriceResponse(
        Integer tradePointId,
        Integer productId,
        BigDecimal price
) {
}
