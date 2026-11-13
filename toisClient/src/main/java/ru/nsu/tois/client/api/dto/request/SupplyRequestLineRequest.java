package ru.nsu.tois.client.api.dto.request;

public record SupplyRequestLineRequest(
        Integer productId,
        String productName,
        String units,
        Integer requestedQuantity
) {
}
