package ru.nsu.tois.dto.response;

import java.time.LocalDate;

public record SaleResponse(
        Integer id,
        LocalDate saleDate,
        Integer tradePointId,
        Integer sellerId,
        Integer customerId
) {
}
