package ru.nsu.tois.dto.response;

public record TradePointProductStockResponse(
        Integer tradePointId,
        Integer productId,
        Integer quantity
) {
}
