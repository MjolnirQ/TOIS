package ru.nsu.tois.dto.response;

import java.util.List;

public record SupplyRequestWithItemsResponse(
        SupplyRequestResponse supplyRequest,
        List<SupplyRequestItemResponse> items
) {
}
