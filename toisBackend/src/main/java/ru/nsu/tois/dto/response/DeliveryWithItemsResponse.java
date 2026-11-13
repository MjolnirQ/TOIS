package ru.nsu.tois.dto.response;

import java.util.List;

public record DeliveryWithItemsResponse(
        DeliveryResponse delivery,
        List<DeliveryItemResponse> items
) {
}
