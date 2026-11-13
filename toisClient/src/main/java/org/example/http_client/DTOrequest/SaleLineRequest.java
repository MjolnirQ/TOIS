package org.example.http_client.DTOrequest;

public record SaleLineRequest(
        Integer productId,
        Integer quantity
) {
}
