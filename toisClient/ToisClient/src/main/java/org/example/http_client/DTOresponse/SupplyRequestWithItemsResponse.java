package org.example.http_client.DTOresponse;

import java.util.List;

public record SupplyRequestWithItemsResponse(
        SupplyRequestResponse supplyRequest,
        List<SupplyRequestItemResponse> items
) {
}
