package ru.nsu.tois.client.api.dto.request;

public record SaleLineRequest(
        Integer productId,
        Integer quantity
) {
}
