package ru.nsu.tois.dto.response;

public record ProductTransferItemResponse(
        Integer id,
        Integer productTransferId,
        Integer productId,
        Integer quantity
) {
}
