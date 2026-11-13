package org.example.http_client.DTOresponse;

public record SupplyRequestResponse(
        Integer id,
        String requestDate,
        Integer tradePointId,
        String status
) {
}
