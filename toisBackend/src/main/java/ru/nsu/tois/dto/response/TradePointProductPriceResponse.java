package ru.nsu.tois.dto.response;

import java.math.BigDecimal;

public record TradePointProductPriceResponse(
        Integer tradePointId,
        Integer productId,
        BigDecimal price
) {
}
