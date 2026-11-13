package org.example.http_client.DTOresponse;

public record SaleResponse(
        Integer id,
        String saleDate,
        Integer tradePointId,
        Integer sellerId,
        Integer customerId
) {
}
