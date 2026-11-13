package ru.nsu.tois.dto.response;

import java.util.List;

public record SaleWithItemsResponse(
        SaleResponse sale,
        List<SaleItemResponse> items
) {
}
