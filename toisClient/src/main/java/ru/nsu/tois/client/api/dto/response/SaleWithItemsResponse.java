package ru.nsu.tois.client.api.dto.response;

import java.util.List;

public record SaleWithItemsResponse(
        SaleResponse sale,
        List<SaleItemResponse> items
) {
}
