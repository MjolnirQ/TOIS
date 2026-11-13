package ru.nsu.tois.client.api.dto.response;

public record SupplyRequestResponse(
        Integer id,
        String requestDate,
        Integer tradePointId,
        String status
) {
}
