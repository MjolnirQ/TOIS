package ru.nsu.tois.dto.response;

public record SupplyRequestItemResponse(
        Integer id,
        Integer supplyRequestId,
        Integer productId,
        Integer requestedQuantity
) {
}
