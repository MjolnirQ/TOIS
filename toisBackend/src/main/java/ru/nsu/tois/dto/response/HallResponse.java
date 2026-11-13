package ru.nsu.tois.dto.response;

import java.math.BigDecimal;

public record HallResponse(
        Integer id,
        Integer tradePointId,
        Integer floor,
        BigDecimal area
) {
}
