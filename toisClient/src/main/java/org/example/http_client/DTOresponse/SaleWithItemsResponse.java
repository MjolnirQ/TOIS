package org.example.http_client.DTOresponse;

import java.util.List;

public record SaleWithItemsResponse(
        SaleResponse sale,
        List<SaleItemResponse> items
) {
}
