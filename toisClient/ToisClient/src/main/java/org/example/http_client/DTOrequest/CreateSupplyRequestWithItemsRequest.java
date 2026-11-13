package org.example.http_client.DTOrequest;

import java.util.List;

public record CreateSupplyRequestWithItemsRequest(
        String requestDate,
        Integer tradePointId,
        String status,
        List<SupplyRequestLineRequest> items
) {
}
