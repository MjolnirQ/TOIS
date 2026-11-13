package ru.nsu.tois.client.api.dto.request;

import java.util.List;

public record CreateSupplyRequestWithItemsRequest(
        String requestDate,
        Integer tradePointId,
        String status,
        List<SupplyRequestLineRequest> items
) {
}
