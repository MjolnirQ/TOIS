package org.example.http_client.DTOrequest;

public record SupplyRequestLineRequest(
        Integer productId,
        String productName,
        String units,
        Integer requestedQuantity
) {
}
