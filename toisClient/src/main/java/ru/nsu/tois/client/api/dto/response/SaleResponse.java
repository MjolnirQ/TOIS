package ru.nsu.tois.client.api.dto.response;

public record SaleResponse(
        Integer id,
        String saleDate,
        Integer tradePointId,
        Integer sellerId,
        Integer customerId
) {
}
