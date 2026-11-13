package ru.nsu.tois.dto.response;

public record SellerAssignmentResponse(
        Integer id,
        Integer sellerId,
        Integer tradePointId,
        Integer hallId
) {
}
