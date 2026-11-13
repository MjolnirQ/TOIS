package org.example.http_client.DTOresponse;

public record SupplyRequestItemResponse(
        Integer id,
        Integer supplyRequestId,
        Integer productId,
        Integer requestedQuantity
) {
}
