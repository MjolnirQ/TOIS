package ru.nsu.tois.dto.response;

import java.time.LocalDate;

public record SupplyRequestResponse(
        Integer id,
        LocalDate requestDate,
        Integer tradePointId,
        String status
) {
}
