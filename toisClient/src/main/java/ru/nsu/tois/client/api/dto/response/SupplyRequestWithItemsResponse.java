package ru.nsu.tois.client.api.dto.response;

import java.util.List;

public record SupplyRequestWithItemsResponse(
        SupplyRequestResponse supplyRequest,
        List<SupplyRequestItemResponse> items
) {
}
