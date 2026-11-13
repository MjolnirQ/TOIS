package ru.nsu.tois.dto.response;

import java.math.BigDecimal;

public record DeliveryItemResponse(
        Integer id,
        Integer deliveryId,
        Integer productId,
        Integer quantity,
        BigDecimal deliveryPrice
) {
}
