package ru.nsu.tois.dto.response;

import java.util.List;

public record ProductTransferWithItemsResponse(
        ProductTransferResponse productTransfer,
        List<ProductTransferItemResponse> items
) {
}
