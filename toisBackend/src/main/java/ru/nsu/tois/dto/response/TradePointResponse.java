package ru.nsu.tois.dto.response;

import ru.nsu.tois.entity.tradePoints.TradePointType;

import java.math.BigDecimal;

public record TradePointResponse(
        Integer id,
        String name,
        String address,
        TradePointType type,
        BigDecimal tradeArea,
        BigDecimal utilityPayments,
        Integer counterCount,
        BigDecimal rentPayment
) {
}
