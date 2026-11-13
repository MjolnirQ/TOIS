package ru.nsu.tois.client.api.dto.response;

public record SupplyRequestItemResponse(
        Integer id,
        Integer supplyRequestId,
        Integer productId,
        Integer requestedQuantity
) {
}
