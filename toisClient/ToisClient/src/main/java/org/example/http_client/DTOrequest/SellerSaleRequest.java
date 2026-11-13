package org.example.http_client.DTOrequest;

import java.util.List;

public record SellerSaleRequest(
        String saleDate,
        Integer tradePointId,
        Integer customerId,
        List<SaleLineRequest> items
) {
}
